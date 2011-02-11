package glyph.test.leasing;

import org.jini.glyph.LeasedResource;

@LeasedResource
public class ListenerResource {
	private TimeListener listener;
	public ListenerResource(TimeListener l){
		this.listener = l;
	}
	public TimeListener getListener() {
		return listener;
	}
}
