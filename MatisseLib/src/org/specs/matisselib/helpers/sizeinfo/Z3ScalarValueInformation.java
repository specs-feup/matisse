/**
 * Copyright 2016 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package org.specs.matisselib.helpers.sizeinfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.specs.CIR.Types.VariableType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixType;
import org.specs.CIR.Types.ATypes.Matrix.MatrixUtils;
import org.specs.CIR.Types.ATypes.Scalar.ScalarType;
import org.specs.matisselib.ssa.instructions.FunctionCallInstruction;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntSort;
import com.microsoft.z3.Params;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;

import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.z3helper.ContextHolder;
import pt.up.fe.specs.z3helper.UnsupportedPlatformException;
import pt.up.fe.specs.z3helper.Z3LibraryLoader;

public class Z3ScalarValueInformation extends ScalarValueInformation {

    private static final boolean PRINT_MODEL = false;
    private static final int SOFT_TIMEOUT = 10;

    private final Function<String, Optional<VariableType>> typeGetter;
    private final ContextHolder context;
    private final Solver solver;
    private final Map<String, ArithExpr> symbols;
    private boolean closed;

    public Z3ScalarValueInformation(Function<String, Optional<VariableType>> typeGetter) {
        try {
            Z3LibraryLoader.loadNativeLibraries();
        } catch (IOException | UnsupportedPlatformException e) {
            throw new RuntimeException(e);
        }

        this.typeGetter = typeGetter;
        this.context = new ContextHolder();
        this.solver = makeSolver();
        this.symbols = new HashMap<>();
    }

    private Z3ScalarValueInformation(Z3ScalarValueInformation other) {
        this.typeGetter = other.typeGetter;
        this.context = other.context;
        this.context.addRef();
        this.solver = makeSolver();
        for (BoolExpr assertion : other.solver.getAssertions()) {
            this.solver.add(assertion);
        }
        this.symbols = new HashMap<>(other.symbols);
    }

    private Solver makeSolver() {
        Solver solver = this.context.mkSolver();
        Params params = this.context.mkParams();
        params.add("solver2_timeout", SOFT_TIMEOUT);

        solver.setParameters(params);
        return solver;
    }

    @Override
    public void close() {
        closed = true;

        this.context.removeRef();
    }

    private void checkValid() {
        if (closed) {
            throw new IllegalStateException("Using closed information");
        }
    }

    @Override
    public Z3ScalarValueInformation copy() {
        checkValid();

        return new Z3ScalarValueInformation(this);
    }

    @Override
    public void buildScalarCopy(String outScalar, String inScalar) {
        checkValid();

        this.solver.add(this.context.mkEq(getSymbol(outScalar), getSymbol(inScalar)));
    }

    @Override
    public void addAlias(String oldValue, String newValue) {
        checkValid();

        this.symbols.put(newValue, getSymbol(oldValue));
    }

    private ArithExpr getSymbol(String name) {
        checkValid();

        return getSymbol(name, null);
    }

    private ArithExpr getSymbol(String originalName, String context) {
        String name = originalName;
        if (context != null) {
            name += "#" + context;
        } else if (originalName.contains("#")) {
            originalName = originalName.substring(0, originalName.indexOf('#'));
        }

        if (this.symbols.containsKey(name)) {
            ArithExpr arithExpr = this.symbols.get(name);
            return arithExpr;
        }

        ScalarType type = this.typeGetter.apply(originalName)
                .filter(ScalarType.class::isInstance)
                .map(ScalarType.class::cast)
                .orElse(null);

        Sort variableSort = type == null || !type.scalar().isInteger() ? this.context.mkRealSort()
                : this.context.mkIntSort();

        ArithExpr symbol = (ArithExpr) this.context.mkConst(name,
                variableSort);

        this.symbols.put(name, symbol);

        if (type != null && type.scalar().hasConstant()) {
            double value = type.scalar().getConstant().doubleValue();
            specifyConstant(name, value);
        }

        return symbol;
    }

    @Override
    public boolean areSameValue(String v1, String v2) {
        checkValid();

        BoolExpr exprToTest = this.context.mkEq(getSymbol(v1), getSymbol(v2));
        return isNecessary(exprToTest,
                "Could not prove whether " + v1 + " = " + v2 + " due to limitations in the Z3 solver.");
    }

    private boolean isNecessary(BoolExpr exprToTest, String failureMessage) {
        this.solver.push();
        this.solver.add(this.context.mkNot(exprToTest));
        Status status = runSolver();
        this.solver.pop();

        if (status == Status.UNKNOWN) {
            SpecsLogs
                    .msgWarn(failureMessage);
        }

        return status == Status.UNSATISFIABLE;
    }

    @SuppressWarnings("unused")
    private boolean isPossible(BoolExpr exprToTest, String failureMessage) {
        this.solver.push();
        this.solver.add(exprToTest);

        Status status = runSolver();

        if (status == Status.UNKNOWN) {
            SpecsLogs
                    .msgWarn(failureMessage);
        }

        if (status == Status.SATISFIABLE && PRINT_MODEL) {
            System.out.println("For: " + exprToTest.simplify());
            System.out.println("Test:");
            System.out.println(solver.getModel());
        }
        this.solver.pop();

        return status == Status.SATISFIABLE;
    }

    private Status runSolver() {
        return this.solver.check();
    }

    @Override
    public boolean isKnownLessOrEqualTo(String v1, String v2) {
        checkValid();

        BoolExpr exprToTest = this.context.mkLe(getSymbol(v1), getSymbol(v2));
        return isNecessary(exprToTest,
                "Could not prove whether " + v1 + " = " + v2 + " due to limitations in the Z3 solver.");
    }

    @Override
    public void setUpTo(String value, String maximum) {
        setUpTo(value, maximum, null);
    }

    @Override
    public void setUpTo(String value, String maximum, String context) {
        checkValid();

        this.solver.add(this.context.mkLe(getSymbol(value, context), getSymbol(maximum, context)));
    }

    @Override
    public void setTrue(String value) {
        this.solver.add(this.context.mkNe(getSymbol(value), mkIntConst(0)));
    }

    @Override
    public void setAtLeast(String value, String minimum) {
        setAtLeast(value, minimum, null);
    }

    @Override
    public void setAtLeast(String value, String minimum, String context) {
        checkValid();

        setUpTo(minimum, value, context);
    }

    @Override
    public void specifyConstant(String var, double value) {
        checkValid();

        ArithExpr symbol = getSymbol(var);

        Sort sort = symbol.getSort();
        if (sort instanceof IntSort && value != (int) value) {
            value = (int) value;
        }

        this.solver.add(this.context.mkEq(symbol,
                this.context.mkNumeral(Double.toString(value), sort)));
    }

    @Override
    public void addScalarFunctionCallInformation(FunctionCallInstruction functionCall, String context) {
        checkValid();

        if (functionCall.getOutputs().size() != 1) {
            return;
        }

        String output = functionCall.getOutputs().get(0);

        switch (functionCall.getFunctionName()) {
        case "minus": {

            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            this.solver.add(
                    this.context.mkEq(getSymbol(output, context),
                            this.context.mkSub(getSymbol(in1, context), getSymbol(in2, context))));
            break;
        }
        case "plus": {

            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    this.context.mkAdd(getSymbol(in1, context), getSymbol(in2, context))));

            break;
        }

        case "times": {

            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    this.context.mkMul(getSymbol(in1, context), getSymbol(in2, context))));

            break;
        }
        case "max": {
            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    mkMax(getSymbol(in1, context), getSymbol(in2, context))));

            break;
        }
        case "ndims": {
            if (functionCall.getInputVariables().size() != 1) {
                return;
            }

            String input = functionCall.getInputVariables().get(0);
            int rawNdims = this.typeGetter.apply(input)
                    .filter(MatrixType.class::isInstance)
                    .map(MatrixUtils::getShape)
                    .map(shape -> shape.getRawNumDims())
                    .orElse(-1);

            this.solver.add(this.context.mkGe(getSymbol(output, context), mkIntConst(2)));
            if (rawNdims > 2) {
                this.solver.add(this.context.mkLe(getSymbol(output, context), mkIntConst(rawNdims)));
            }

            break;
        }
        case "le": {
            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            ArithExpr trueValue = mkIntConst(1);
            ArithExpr falseValue = mkIntConst(0);
            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    this.context.mkITE(this.context.mkLe(getSymbol(in1, context), getSymbol(in2, context)), trueValue,
                            falseValue)));

            break;
        }
        case "eq": {
            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            ArithExpr trueValue = mkIntConst(1);
            ArithExpr falseValue = mkIntConst(0);
            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    this.context.mkITE(this.context.mkEq(getSymbol(in1, context), getSymbol(in2, context)),
                            trueValue,
                            falseValue)));

            break;
        }
        case "ne": {
            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            String in1 = functionCall.getInputVariables().get(0);
            String in2 = functionCall.getInputVariables().get(1);

            ArithExpr trueValue = mkIntConst(1);
            ArithExpr falseValue = mkIntConst(0);
            this.solver.add(this.context.mkEq(getSymbol(output, context),
                    this.context.mkITE(this.context.mkEq(getSymbol(in1, context), getSymbol(in2, context)),
                            falseValue,
                            trueValue)));

            break;
        }
        case "size":
            if (functionCall.getInputVariables().size() != 2) {
                return;
            }

            this.solver.add(this.context.mkGe(getSymbol(output, context), mkIntConst(0)));
            break;
        case "numel":
            if (functionCall.getInputVariables().size() != 1) {
                return;
            }

            this.solver.add(this.context.mkGe(getSymbol(output, context), mkIntConst(0)));
            break;
        default:
            // Do nothing
        }
    }

    private ArithExpr mkIntConst(int value) {
        return (ArithExpr) this.context.mkNumeral(value, this.context.mkIntSort());
    }

    @Override
    public void setRangeSize(String size, String start, String end) {
        checkValid();

        ArithExpr diff = this.context.mkSub(getSymbol(end), getSymbol(start));
        ArithExpr one = (ArithExpr) this.context.mkNumeral(1, this.context.mkIntSort());
        ArithExpr diffPlusOne = this.context.mkAdd(diff, one);
        Expr sizeValue = mkMax(diffPlusOne, one);

        this.solver.add(this.context.mkEq(getSymbol(size), sizeValue));
    }

    private Expr mkMax(ArithExpr expr1, ArithExpr expr2) {
        BoolExpr comparison = this.context.mkGe(expr1, expr2);
        return this.context.mkITE(comparison, expr1, expr2);
    }

    private BoolExpr mkNe(Expr expr1, Expr expr2) {
        BoolExpr eq = this.context.mkEq(expr1, expr2);
        return this.context.mkNot(eq);
    }

    @Override
    public String toString() {
        checkValid();

        StringBuilder builder = new StringBuilder("[Z3 Solver:");
        for (BoolExpr assertion : this.solver.getAssertions()) {
            builder.append("\n\t");
            builder.append(assertion);
        }
        builder.append("\n]");

        return builder.toString();
    }

    @Override
    public boolean isKnownLessThan(String v1, String v2) {
        checkValid();

        return isNecessary(
                this.context.mkLt(getSymbol(v1), getSymbol(v2)),
                "Could not determine whether " + v1 + " < " + v2);
    }

    @Override
    public boolean isKnownEqual(String var, int i) {
        checkValid();

        return isNecessary(this.context.mkEq(getSymbol(var), this.context.mkNumeral(i, this.context.mkIntSort())),
                "Could not determine whether " + var + " == " + i);
    }

    @Override
    public boolean isKnownNotEqual(String var, int i) {
        checkValid();

        return isNecessary(mkNe(getSymbol(var), this.context.mkNumeral(i, this.context.mkIntSort())),
                "Could not determine whether " + var + " != " + i);
    }

    @Override
    public boolean isKnownPositive(String var) {
        checkValid();

        return isKnownGreaterThan(var, 0);
    }

    @Override
    public boolean isKnownNegative(String var) {
        checkValid();

        return isKnownLessThan(var, 0);
    }

    private boolean isKnownGreaterThan(String var, int i) {
        return isNecessary(
                this.context.mkGt(getSymbol(var), (ArithExpr) this.context.mkNumeral(i, this.context.mkIntSort())),
                "Could not determine whether " + var + " > " + i);
    }

    private boolean isKnownLessThan(String var, int i) {
        return isNecessary(
                this.context.mkLt(getSymbol(var), (ArithExpr) this.context.mkNumeral(i, this.context.mkIntSort())),
                "Could not determine whether " + var + " < " + i);
    }

    @Override
    public boolean growsWith(List<String> values, List<String> reference, String ctx1, String ctx2) {
        checkValid();

        BoolExpr referenceExpr = this.context.mkTrue();

        for (String refVar : reference) {
            BoolExpr partialExpr = this.context.mkGe(getSymbol(refVar, ctx1), getSymbol(refVar, ctx2));

            referenceExpr = this.context.mkAnd(referenceExpr, partialExpr);
        }

        BoolExpr valueExpr = this.context.mkTrue();

        for (String value : values) {
            BoolExpr partialExpr = this.context.mkGe(getSymbol(value, ctx1), getSymbol(value, ctx2));

            valueExpr = this.context.mkAnd(valueExpr, partialExpr);
        }

        BoolExpr conditionToCheck = this.context.mkImplies(referenceExpr, valueExpr);

        return isNecessary(conditionToCheck, "Could not check whether " + values + " grows with " + reference);
    }

    @Override
    public boolean mayCollide(List<String> values1, List<String> values2, List<String> reference, String ctx1,
            String ctx2) {

        checkValid();

        if (values1.size() != values2.size()) {
            return true;
        }

        BoolExpr referenceExpr = this.context.mkFalse();

        for (String refVar : reference) {
            BoolExpr partialExpr = mkNe(getSymbol(refVar, ctx1), getSymbol(refVar, ctx2));

            referenceExpr = this.context.mkOr(referenceExpr, partialExpr);
        }

        BoolExpr valueExpr = this.context.mkTrue();

        for (int i = 0; i < values1.size(); i++) {
            String value1 = values1.get(i);
            String value2 = values2.get(i);
            BoolExpr partialExpr = this.context.mkEq(getSymbol(value1, ctx1), getSymbol(value2, ctx2));

            valueExpr = this.context.mkAnd(valueExpr, partialExpr);
        }

        BoolExpr conditionToCheck = this.context.mkAnd(referenceExpr, valueExpr);

        return isPossible(conditionToCheck,
                "Could not check whether " + values1 + ", " + values2 + " may collide with reference: " + reference);
    }
}
