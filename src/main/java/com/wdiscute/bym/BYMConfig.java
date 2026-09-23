package com.wdiscute.bym;

import net.neoforged.neoforge.common.ModConfigSpec;

public class  BYMConfig
{
    public static class Client
    {
        private static final ModConfigSpec.Builder BUILDER_CLIENT = new ModConfigSpec.Builder();

        //public static final ModConfigSpec.BooleanValue ENABLE_MISS_SOUND = BUILDER_CLIENT
        //        .comment("Should play the hit sound")
        //        .translation("starcatcher.configuration.enable_miss_sound")
        //        .define("enable_miss_sound", true);


        static final ModConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();
    }

    public static class Server
    {
        private static final ModConfigSpec.Builder BUILDER_SERVER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.IntValue TIME_INSIDE_MIND = BUILDER_SERVER
                .comment("How long a player can stay inside the mind")
                .defineInRange("time_inside_mind", 6000, 0, Integer.MAX_VALUE);

        static final ModConfigSpec SPEC_SERVER = BUILDER_SERVER.build();
    }
}
