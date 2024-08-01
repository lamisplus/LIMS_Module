package org.lamisplus.modules.lims.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(3)
@Installer(name = "lims-schema-installer-update-2",
        description = "LIMS installer updates 2",
        version = 2)
public class LimsSchemaInstallerUpdate2 extends AcrossLiquibaseInstaller {
    public LimsSchemaInstallerUpdate2() {
        super("classpath:installers/lims/schema/schema-update-2.xml");
    }
}
