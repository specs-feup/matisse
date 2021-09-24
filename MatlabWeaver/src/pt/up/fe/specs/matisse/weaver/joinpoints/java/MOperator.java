/**
 * Copyright 2015 SPeCS.
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

package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.util.List;
import java.util.stream.Collectors;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.OperatorNode;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperand;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AOperator;
import pt.up.fe.specs.util.SpecsLogs;

public class MOperator extends AOperator {

    private final OperatorNode operator;

    public MOperator(OperatorNode operator, AMWeaverJoinPoint parent) {
        super(new MExpression(operator, parent));
        initMWeaverJP(parent);

        this.operator = operator;
    }

    @Override
    public List<? extends AOperand> selectOperand() {
        return operator.getChildrenStream()
                .map(child -> new MOperand(child, this))
                .collect(Collectors.toList());

    }

    @Override
    public Integer getArityImpl() {
        return operator.getOp().getNumOperands();
    }

    @Override
    public String getSymbolImpl() {
        return operator.getOp().getMatlabString();
    }

    @Override
    public Long getUidImpl() {
        return (long) operator.hashCode();
    }

    @Override
    public MatlabNode getNode() {
        return operator;
    }

    @Override
    public String getLeftOperandImpl() {
        if (operator.getNumOperands() < 1) {
            SpecsLogs.msgInfo("[MWeaver-error] Operator '" + operator.getOp() + "' does not have a left operand");
            return null;
        }

        return operator.getOperands().get(0).getCode();
    }

    @Override
    public String getRightOperandImpl() {
        if (operator.getNumOperands() < 2) {
            SpecsLogs.msgInfo("[MWeaver-error] Operator '" + operator.getOp() + "' does not have a right operand");
            return null;
        }

        return operator.getOperands().get(1).getCode();
    }

    @Override
    public String[] getOperandsArrayImpl() {
        return operator.getOperands().stream()
                .map(operand -> operand.getCode())
                .toArray(String[]::new);
    }

}
