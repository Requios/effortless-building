//package nl.requios.effortlessbuilding.create.foundation.utility;
//
//import net.minecraft.resources.ResourceLocation;
//import net.neoforged.neoforge.registries.IForgeRegistry;
//import nl.requios.effortlessbuilding.create.Create;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.*;
//
//public class CreateRegistry<K, V> {
//	private static final List<CreateRegistry<?, ?>> ALL = new ArrayList<>();
//
//	protected final IForgeRegistry<K> objectRegistry;
//	protected final Map<ResourceLocation, V> locationMap = new HashMap<>();
//	protected final Map<K, V> objectMap = new IdentityHashMap<>();
//	protected boolean unwrapped = false;
//
//	public CreateRegistry(IForgeRegistry<K> objectRegistry) {
//		this.objectRegistry = objectRegistry;
//		ALL.add(this);
//	}
//
//	public void register(ResourceLocation location, V value) {
//		if (!unwrapped) {
//			locationMap.put(location, value);
//		} else {
//			K object = objectRegistry.getValue(location);
//			if (object != null) {
//				objectMap.put(object, value);
//			} else {
//				Create.LOGGER.warn("Could not get object for location '" + location + "' in CreateRegistry after unwrapping!");
//			}
//		}
//	}
//
//	public void register(K object, V value) {
//		if (unwrapped) {
//			objectMap.put(object, value);
//		} else {
//			ResourceLocation location = objectRegistry.getKey(object);
//			if (location != null) {
//				locationMap.put(location, value);
//			} else {
//				Create.LOGGER.warn("Could not get location of object '" + object + "' in CreateRegistry before unwrapping!");
//			}
//		}
//	}
//
//	@Nullable
//	public V get(ResourceLocation location) {
//		if (!unwrapped) {
//			return locationMap.get(location);
//		} else {
//			K object = objectRegistry.getValue(location);
//			if (object != null) {
//				return objectMap.get(object);
//			} else {
//				Create.LOGGER.warn("Could not get object for location '" + location + "' in CreateRegistry after unwrapping!");
//				return null;
//			}
//		}
//	}
//
//	@Nullable
//	public V get(K object) {
//		if (unwrapped) {
//			return objectMap.get(object);
//		} else {
//			ResourceLocation location = objectRegistry.getKey(object);
//			if (location != null) {
//				return locationMap.get(location);
//			} else {
//				Create.LOGGER.warn("Could not get location of object '" + object + "' in CreateRegistry before unwrapping!");
//				return null;
//			}
//		}
//	}
//
//	public boolean isUnwrapped() {
//		return unwrapped;
//	}
//
//	protected void unwrap() {
//		for (Map.Entry<ResourceLocation, V> entry : locationMap.entrySet()) {
//			ResourceLocation location = entry.getKey();
//			K object = objectRegistry.getValue(location);
//			if (object != null) {
//				objectMap.put(object, entry.getValue());
//			} else {
//				Create.LOGGER.warn("Could not get object for location '" + location + "' in CreateRegistry during unwrapping!");
//			}
//		}
//		unwrapped = true;
//	}
//
//	public static void unwrapAll() {
//		for (CreateRegistry<?, ?> registry : ALL) {
//			registry.unwrap();
//		}
//	}
//}
