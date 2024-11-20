package nl.requios.effortlessbuilding.create.foundation.gui.container;

public interface IClearableMenu {

	default void sendClearPacket() {
//		PacketDistributor.SERVER.noArg().send(new ClearMenuPacket());
	}

	public void clearContents();

}
