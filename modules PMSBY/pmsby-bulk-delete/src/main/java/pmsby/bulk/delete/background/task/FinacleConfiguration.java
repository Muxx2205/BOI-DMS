package pmsby.bulk.delete.background.task;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "FinacleConfiguration", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = "pmsby.bulk.delete.background.task.FinacleConfiguration")
public interface FinacleConfiguration {

    @Meta.AD(required = false, deflt = "2000", description = "Number of records per thread")
    public Integer batchSize();
    
    @Meta.AD(required = false, deflt = "4", description = "Number of parallel threads")
    public Integer parallelThreads();
    
    @Meta.AD(required = false, deflt = "2,3", description = "Stage values to process (comma separated, e.g., '2,3')")
    public String stages();
    
    @Meta.AD(required = false, deflt = "12", description = "Evening window start hour (UTC)")
    public Integer eveningStartHour();
    
    @Meta.AD(required = false, deflt = "30", description = "Evening window start minute (UTC)")
    public Integer eveningStartMinute();
    
    @Meta.AD(required = false, deflt = "17", description = "Evening window end hour (UTC)")
    public Integer eveningEndHour();
    
    @Meta.AD(required = false, deflt = "30", description = "Evening window end minute (UTC)")
    public Integer eveningEndMinute();
    
    @Meta.AD(required = false, deflt = "23", description = "Morning window start hour (UTC)")
    public Integer morningStartHour();
    
    @Meta.AD(required = false, deflt = "30", description = "Morning window start minute (UTC)")
    public Integer morningStartMinute();
    
    @Meta.AD(required = false, deflt = "3", description = "Morning window end hour (UTC)")
    public Integer morningEndHour();
    
    @Meta.AD(required = false, deflt = "30", description = "Morning window end minute (UTC)")
    public Integer morningEndMinute();
    
    @Meta.AD(required = false, deflt = "60000", description = "Connection timeout in milliseconds")
    public Integer connectiontimeout();
    
    @Meta.AD(required = false, deflt = "60000", description = "Read timeout in milliseconds")
    public Integer readTimeout();
    
    @Meta.AD(required = false, deflt = "500", description = "Maximum consecutive errors before stopping")
    public Integer maxErrors();
    
    @Meta.AD(required = false, deflt = "https://psms.starfina.boicorp.net:35500/FISERVLET/fihttp", description = "Finacle API URL")
    public String finacleURL();
    
    @Meta.AD(required = false, deflt = "O_46102212172422_PMSBYPMJJYCONSENTFORM", description = "Main table name")
    public String mainTable();
    
    @Meta.AD(required = false, deflt = "O_46102212172422_PMSBYPMJJYCONSENTFORM_X", description = "Extension table name")
    public String extensionTable();
}