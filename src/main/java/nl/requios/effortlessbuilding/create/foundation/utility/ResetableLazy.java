package nl.requios.effortlessbuilding.create.foundation.utility;

import net.neoforged.neoforge.common.util.NonNullSupplier;

import java.util.function.Supplier;

public class ResetableLazy<T> implements Supplier<T> {

	private final NonNullSupplier<T> supplier;
	private T value;

	public ResetableLazy(NonNullSupplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T get() {
		if (value == null) {
			value = supplier.get();
		}
		return value;
	}

	public void reset() {
		value = null;
	}

	public static <T> ResetableLazy<T> of(NonNullSupplier<T> supplier) {
		return new ResetableLazy<>(supplier);
	}

}
