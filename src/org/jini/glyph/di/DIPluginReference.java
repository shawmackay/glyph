package org.jini.glyph.di;

public class DIPluginReference {
	private String name;
	private Object[] arguments;
	public DIPluginReference(String name, Object... arguments) {
		super();
		this.name = name;
		this.arguments = arguments;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object[] getArguments() {
		return arguments;
	}
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
	
}
