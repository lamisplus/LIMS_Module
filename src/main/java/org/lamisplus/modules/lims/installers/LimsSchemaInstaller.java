package org.lamisplus.modules.lims.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(1)
@Installer(name = "lims-schema-installer",
        description = "Installs the required lims database tables",
        version = 1)
public class LimsSchemaInstaller extends AcrossLiquibaseInstaller {
    public LimsSchemaInstaller() {
        super("classpath:installers/lims/schema/schema.xml");
    }
}
