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
import org.stringtemplate.v4.ST;

import static com.qfs.sandbox.cfg.datastore.impl.DatastoreDescriptionConfig.*;

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

    public static final String DATE = "Date";
    public static final String INDEX_NAME = "IndexName";
    public static final String NUMBER_STOCKS = "NumberStocks";
    public static final String STOCK_SYMBOL = "StockSymbol";
    public static final String POSITION_TYPE = "PositionType";

    public static final String OPEN = REF_PORTFOLIO_TO_STOCK + "/Open";
    public static final String HIGH = REF_PORTFOLIO_TO_STOCK + "/High";
    public static final String LOW = REF_PORTFOLIO_TO_STOCK + "/Low";
    public static final String CLOSE = REF_PORTFOLIO_TO_STOCK + "/Close";
    public static final String VOLUME = REF_PORTFOLIO_TO_STOCK + "/Volume";
    public static final String ADJ_CLOSE = REF_PORTFOLIO_TO_STOCK + "/AdjClose";


    public static final String NAME = "Name";
    public static final String SECTOR = "Sector";
    public static final String INDUSTRY = "Industry";


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
                .withField(DATE)
                .withField(INDEX_NAME)
                .withField(NUMBER_STOCKS)
                .withField(STOCK_SYMBOL)
                .withField(POSITION_TYPE)

                .withField(OPEN)
                .withField(HIGH)
                .withField(LOW)
                .withField(CLOSE)
                .withField(VOLUME)
                .withField(ADJ_CLOSE)

                .withField(COMPANY_NAME, REF_PORTFOLIO_TO_COMPANY + "/" + COMPANY_NAME)
                .withField(COMPANY_SECTOR, REF_PORTFOLIO_TO_COMPANY + "/" + COMPANY_SECTOR)
                .withField(COMPANY_INDUSTRY, REF_PORTFOLIO_TO_COMPANY + "/" + COMPANY_INDUSTRY)

                .build();
    }

}
