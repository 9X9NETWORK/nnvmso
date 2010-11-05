package com.nnvmso.model;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@PersistenceCapable(detachable = "true")
public class ProgramScript implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent(mappedBy = "script")
	private MsoProgram program;

	@Persistent
	private Text script;

	public ProgramScript() {
	}

	public MsoProgram getProgram() {
		return program;
	}

	public void setProgram(MsoProgram program) {
		this.program = program;
	}

	public Text getScript() {
		return script;
	}

	public void setScript(Text script) {
		this.script = script;
	}
}
