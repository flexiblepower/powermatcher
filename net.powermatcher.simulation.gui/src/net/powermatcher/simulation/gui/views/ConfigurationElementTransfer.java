package net.powermatcher.simulation.gui.views;

import org.eclipse.swt.dnd.ByteArrayTransfer;

public class ConfigurationElementTransfer extends ByteArrayTransfer {
	// @Override
	// public TransferData[] getSupportedTypes() {
	// return new TransferData[] { new ConfigurationElementTransferData() };
	// }

	@Override
	protected int[] getTypeIds() {
		return new int[] { 1 };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { "ConfigurationElement" };
	}

	// @Override
	// public boolean isSupportedType(TransferData data) {
	// return data instanceof ConfigurationElementTransferData;
	// }

//	@Override
//	protected void javaToNative(Object object, TransferData data) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	protected Object nativeToJava(TransferData data) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
}
