/*
 * (C) ActiveViam 2017
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.qfs.sandbox.cfg.role.impl;

import com.activeviam.builders.StartBuilding;
import com.qfs.sandbox.context.impl.CurrencyContextValue;
import com.qfs.sandbox.security.impl.ASecurityConfig;
import com.qfs.server.cfg.IRoleContextConfig;
import com.quartetfs.biz.pivot.security.IEntitlementsProvider;
import com.quartetfs.biz.pivot.security.builders.ICanHaveRoleEntitlements.ICanStartBuildingEntitlement;
import com.quartetfs.biz.pivot.security.builders.ICanHaveRoleEntitlements.IHasAtLeastOneEntitlement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Defines an {@link com.quartetfs.biz.pivot.security.impl.InMemoryEntitlementsProvider in-memory entitlement provider}
 * that keeps the context value for the roles of users.
 * @author ActiveViam
 */
@Configuration
public class RoleContextConfig implements IRoleContextConfig {

	public static final String REF_CURRENCY = "EUR";

	/** ROLE_ADMIN name */
	protected static final String ROLE_ADMIN = ASecurityConfig.ROLE_ADMIN;
	/** ROLE_DESK_A name */
	protected static final String ROLE_DESK_A = "ROLE_DESK_A";
	/** ROLE_EUR_USD name */
	protected static final String ROLE_EUR_USD = "ROLE_EUR_USD";

	/** Name of Equity derivatives cube */
	//protected static final String EQUITY_DERIVATIVES_CUBE = EquityDerivativesCubeConfig.CUBE_NAME;
	/** Name of Equity derivatives distributed cube */
	//protected static final String EQUITY_DERIVATIVES_CUBE_DIST = EquityDerivativesCubeDistConfig.CUBE_NAME;

	/**
	 * Defines the context values for {@link #ROLE_ADMIN}
	 *
	 * @param builder The entitlement builder.
	 * @return the builder for chaining.
	 */
	protected static IHasAtLeastOneEntitlement roleAdmin(final ICanStartBuildingEntitlement builder) {
		return builder
				.withGlobalEntitlement()
						.forRole(ROLE_ADMIN)
						.withContextValue(new CurrencyContextValue(REF_CURRENCY))
						.withMdxContext()
								.withCubeFormatter("en-US")
								.end()
		;
	}

	/**
	 * Defines the context values for {@link #ROLE_DESK_A}
	 *
	 * @param builder The entitlement builder.
	 * @return the builder for chaining.
	 */
	/*protected static IHasAtLeastOneEntitlement roleDeskA(final ICanStartBuildingEntitlement builder) {
		final ISubCubeProperties subCubProperties =
				StartBuilding.subCubeProperties()
						.grantMembers()
								.onDimension(BOOKING_DIMENSION)
								.onHierarchy(TRADE__DESK)
								.memberPath(ILevel.ALLMEMBER)
								.memberPath("DeskA")
				.build();
		return builder
				.withGlobalEntitlement()
						.forRole(ROLE_DESK_A)
						.withMdxContext()
								.withCubeFormatter("fr-FR")
								.end()
				.withPivotEntitlement()
						.forRole(ROLE_DESK_A)
						.onPivot(EQUITY_DERIVATIVES_CUBE)
						.withContextValue(new ReferenceCurrency("USD"))
						.withContextValue(subCubProperties)
				.withPivotEntitlement()
						.forRole(ROLE_DESK_A)
						.onPivot(EQUITY_DERIVATIVES_CUBE_DIST)
						.withContextValue(new ReferenceCurrency("USD"))
						// Clone the context value so that modification to the one of this entitlement does not appear on the one of the other entitlement
						.withContextValue(subCubProperties.clone())
		;
	}*/

	/**
	 * Defines the context values for {@link #ROLE_EUR_USD}
	 * @param builder The entitlement builder.
	 * @return the builder for chaining.
	 */
	/*protected static IHasAtLeastOneEntitlement roleEURUSD(final ICanStartBuildingEntitlement builder) {
		final ISubCubeProperties subCubProperties =
				StartBuilding.subCubeProperties()
						.grantMembers()
								.onDimension(CURRENCY)
								.onHierarchy(CURRENCY)
								.memberPath(ILevel.ALLMEMBER)
								.memberPath("USD")
						.grantMembers()
								.onDimension(CURRENCY)
								.onHierarchy(CURRENCY)
								.memberPath(ILevel.ALLMEMBER)
								.memberPath("EUR")
				.build();
		final IMdxContext mdxContext = StartBuilding.mdxContext()
				.withMeasureAlias(PNL_SUM, "PnL")
				.withMeasureAlias(PNL_FOREX, "PnL FX")
				.withMeasureAlias(PNL_MINIMUM_DEPTH, "PnL AllMemberHide")
				.withMeasureAlias(PNL_DELTA_SUM, "PnL Delta")
				.withMeasureAlias(PNL_VEGA_SUM, "PnL Vega")
				.withMeasureAlias(DELTA_SUM, "Delta")
				.withMeasureAlias(GAMMA_SUM, "Gamma")
				.withMeasureAlias(VEGA_SUM, "Vega")
				.withMeasureAlias(THETA_SUM, "Theta")
				.withMeasureAlias(RHO_SUM, "Rho")
				.withMeasureAlias(PV_SUM, "PV")
				.withMeasureAlias(PV_UNDERLYINGS_RATIO, "PV UnderlyingsRatio")
				.build();
		return builder
				.withPivotEntitlement()
						.forRole(ROLE_EUR_USD)
						.onPivot(EQUITY_DERIVATIVES_CUBE)
						.withContextValue(new ReferenceCurrency("CHF"))
						.withContextValue(mdxContext)
						.withContextValue(subCubProperties)
				.withPivotEntitlement()
						.forRole(ROLE_EUR_USD)
						.onPivot(EQUITY_DERIVATIVES_CUBE_DIST)
						.withContextValue(new ReferenceCurrency("CHF"))
						// Clone the context value so that modification to the one of this entitlement does not appear on the one of the other entitlement
						.withContextValue(mdxContext.clone())
						.withContextValue(subCubProperties.clone())
		;
	}*/

	/**
	 * Builds the {@link IEntitlementsProvider entitlement provider}.
	 * @return the {@link IEntitlementsProvider entitlement provider}.
	 */
	@Bean
	@Override
	public IEntitlementsProvider entitlementsProvider() {
		return StartBuilding.entitlementsProvider()
				.withEntitlements(RoleContextConfig::roleAdmin)
				//.withEntitlements(RoleContextConfig::roleDeskA)
				//.withEntitlements(RoleContextConfig::roleEURUSD)
				.build();
	}
}
