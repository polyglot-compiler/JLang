package polyllvm;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;

public class LLVMScheduler extends Scheduler {

    public LLVMScheduler(ExtensionInfo extInfo) {
        super(extInfo);
    }

    @Override
    public Goal TypeExists(String name) {
        return null;
    }

    @Override
    public Goal MembersAdded(ParsedClassType ct) {
        return null;
    }

    @Override
    public Goal SupertypesResolved(ParsedClassType ct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal SignaturesResolved(ParsedClassType ct) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal FieldConstantsChecked(FieldInstance fi) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal Parsed(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal TypesInitialized(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal TypesInitializedForCommandLine() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ImportTableInitialized(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal SignaturesDisambiguated(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal SupertypesDisambiguated(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal Disambiguated(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal TypeChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ConstantsChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ReachabilityChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ExceptionsChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ExitPathsChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal InitializationsChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ConstructorCallsChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal ForwardReferencesChecked(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal Validated(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal Serialized(Job job) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Goal CodeGenerated(Job job) {
        Goal g = LLVMCodeGenerated.create(this, job);
        return internGoal(g);
    }

}
