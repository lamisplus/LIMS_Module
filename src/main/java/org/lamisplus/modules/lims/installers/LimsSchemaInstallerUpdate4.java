package org.lamisplus.modules.lims.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(5)
@Installer(name = "lims-schema-installer-update-4",
        description = "LIMS installer updates 4",
        version = 1)
public class LimsSchemaInstallerUpdate4 extends AcrossLiquibaseInstaller {
//    public LimsSchemaInstallerUpdate4() {
//        super("classpath:installers/lims/schema/schema-update-4.xml");
//    }
}
