/**
 *
 */
package Utilies;

import com.rits.cloning.Cloner;

import EastWeb_Config.Config;
import EastWeb_Scheduler.SchedulerStatus;
import EastWeb_Summary.ZonalSummary;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import ProjectInfoMetaData.ProjectInfoSummary;

/**
 * @author Michael DeVos
 */
public class EASTWebCloner {
    private static Cloner myClonerInstance = null;

    /**
     * Retrieves an initialized and pre-configured Cloner instance.
     *
     * @return configured Cloner
     */
    public static Cloner GetClonerInstance() {
        if (myClonerInstance == null) {
            myClonerInstance = new Cloner();
            myClonerInstance.registerImmutable(ProjectInfoFile.class);
            myClonerInstance.registerImmutable(ProjectInfoPlugin.class);
            myClonerInstance.registerImmutable(ProjectInfoSummary.class);
            myClonerInstance.registerImmutable(SchedulerStatus.class);
            myClonerInstance.registerImmutable(EASTWebResult.class);
            myClonerInstance.registerImmutable(Config.class);
            myClonerInstance.registerImmutable(ZonalSummary.class);
        }
        return myClonerInstance;
    }

    private EASTWebCloner() {

    }

}
