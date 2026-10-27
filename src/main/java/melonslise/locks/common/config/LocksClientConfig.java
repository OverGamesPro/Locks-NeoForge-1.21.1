package melonslise.locks.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LocksClientConfig
{
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.BooleanValue DEAF_MODE;

	private LocksClientConfig() {}

	static
	{
		ModConfigSpec.Builder cfg = new ModConfigSpec.Builder();

		DEAF_MODE = cfg
			.comment("Display visual feedback when trying to use a locked block for certain hearing impaired individuals")
			.define("Deaf Mode", true);

		SPEC = cfg.build();
	}
}
