package usr.skyswimmer.githubwebhooks.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.util.events.SupplierEventObject;

public interface IStaticSupplierEventDispatcher {

	public Object dispatch(SupplierEventObject<?> event);

}
