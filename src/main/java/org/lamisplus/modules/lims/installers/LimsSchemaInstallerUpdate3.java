package org.lamisplus.modules.lims.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(4)
@Installer(name = "lims-schema-installer-update-3",
        description = "LIMS installer updates 3",
        version = 2)
public class LimsSchemaInstallerUpdate3 extends AcrossLiquibaseInstaller {
    public LimsSchemaInstallerUpdate3() {
        super("classpath:installers/lims/schema/schema-update-3.xml");
    }
}
