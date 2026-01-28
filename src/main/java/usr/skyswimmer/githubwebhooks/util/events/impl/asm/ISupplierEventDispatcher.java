package usr.skyswimmer.githubwebhooks.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.util.events.IEventReceiver;
import usr.skyswimmer.githubwebhooks.util.events.SupplierEventObject;

public interface ISupplierEventDispatcher {

	public Object dispatch(IEventReceiver receiver, SupplierEventObject<?> event);

}
