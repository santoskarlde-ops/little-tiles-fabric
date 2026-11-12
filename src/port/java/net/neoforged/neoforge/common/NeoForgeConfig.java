package net.neoforged.neoforge.common;

/** Minimal holder for the one legacy config write still in the port. */
public final class NeoForgeConfig {

    public static final Server SERVER = new Server();

    private NeoForgeConfig() {
    }

    public static final class Server {
        public final BooleanValue fullBoundingBoxLadders = new BooleanValue();
    }

    public static final class BooleanValue {
        private boolean value;

        public void set(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }
    }
}