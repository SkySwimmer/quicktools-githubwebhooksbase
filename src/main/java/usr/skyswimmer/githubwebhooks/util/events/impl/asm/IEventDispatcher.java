package usr.skyswimmer.githubwebhooks.util.events.impl.asm;

import usr.skyswimmer.githubwebhooks.util.events.EventObject;
import usr.skyswimmer.githubwebhooks.util.events.IEventReceiver;

public interface IEventDispatcher {

	public void dispatch(IEventReceiver receiver, EventObject event);

}
