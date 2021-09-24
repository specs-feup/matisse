package pt.up.fe.specs.matisse.weaver.joinpoints.java;

import java.util.List;

import org.specs.MatlabIR.MatlabNode.MatlabNode;
import org.specs.MatlabIR.MatlabNode.nodes.core.IdentifierNode;
import org.specs.MatlabIR.MatlabNode.nodes.statements.GlobalSt;

import pt.up.fe.specs.matisse.weaver.abstracts.AMWeaverJoinPoint;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AGlobalDeclaration;
import pt.up.fe.specs.matisse.weaver.abstracts.joinpoints.AVar;
import pt.up.fe.specs.matisse.weaver.joinpoints.MJoinpointUtils;

public class MGlobalDeclaration extends AGlobalDeclaration {

    private final GlobalSt globalToken;

    public MGlobalDeclaration(GlobalSt globalToken, AMWeaverJoinPoint parent) {
        super(new MStatement(globalToken, parent));

        this.globalToken = globalToken;
    }

    @Override
    public List<? extends AVar> selectVar() {
        List<IdentifierNode> ids = globalToken.getIdentifierNodes();

        return MJoinpointUtils.getVars(this, ids);
    }

    @Override
    public MatlabNode getNode() {
        return globalToken;
    }

    @Override
    public AMWeaverJoinPoint getParentImpl() {
        return getSuper().get().getParentImpl();
    }

    @Override
    public String toString() {
        return "Global(" + globalToken.getCode() + ")";
    }

}
