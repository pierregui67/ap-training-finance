package com.qfs.sandbox.cfg.pivot.impl;

import com.activeviam.builders.StartBuilding;
import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.server.cfg.IActivePivotManagerConfig;
import com.quartetfs.biz.pivot.definitions.IActivePivotManagerDescription;
import com.quartetfs.biz.pivot.definitions.IActivePivotSchemaDescription;
import com.quartetfs.biz.pivot.definitions.ICatalogDescription;
import com.quartetfs.biz.pivot.definitions.ISelectionDescription;
import com.quartetfs.biz.pivot.impl.ActivePivotManager;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.PORTFOLIOS_STORE_NAME;

@Configuration // And not @Component : solved the problem !
public class PerformanceCubeManagerConfig implements IActivePivotManagerConfig {

    /** Spring environment, automatically wired */
    @Autowired
    protected Environment env;

    /** The datastore schema {@link IDatastoreSchemaDescription description}.  */
    @Autowired
    protected IDatastoreSchemaDescription datastoreDescription;

    /** The name of the {@link ActivePivotManager} */
    public static final String MANAGER_NAME = "Manager";

    /** The name of the {@link ICatalogDescription}. */
    public static final String CATALOG_NAME = "Catalog";

    /** Name of the Sandbox {@link IActivePivotSchemaDescription schema} */
    public static final String SANDBOX_SCHEMA_NAME = "SandboxSchema";

    @Bean
    @Override
    public IActivePivotManagerDescription managerDescription() {
        return StartBuilding.managerDescription(MANAGER_NAME)
                .withCatalog(CATALOG_NAME)
                .containingAllCubes()
                .withSchema(SANDBOX_SCHEMA_NAME)
                .withSelection(createSandboxSchemaSelectionDescription(this.datastoreDescription))
                .withCube(PerformanceCubeConfig.createCubeDescription(false))
                .build();
    }

    /**
     * Creates the {@link ISelectionDescription} for the Sandbox schema.
     *
     * @param datastoreDescription The datastore description
     * @return The created selection description
     */
    public static ISelectionDescription createSandboxSchemaSelectionDescription(
            final IDatastoreSchemaDescription datastoreDescription)
    {
        return StartBuilding.selection(datastoreDescription)
                .fromBaseStore(PORTFOLIOS_STORE_NAME)
                .withField("StockSymbol")
                .withField("NumberStocks")
                .withField("PortfoliosToStockPriceHistory/Volume")
                //.withAllReachableFields()
                //.withAlias(PORTFOLIOS_STOCK_SYMBOL, HISTORY_STOCK_SYMBOL)
                //.withAlias(TRADE__PRODUCT_ID, PRODUCT_ID)
                .build();
    }

}
