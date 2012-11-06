package net.slipp.domain.qna;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Answerer {
	@Enumerated(EnumType.STRING)
	@Column(name = "sns_type", nullable = true, updatable = false, columnDefinition = SnsType.COLUMN_DEFINITION)
	private SnsType snsType;
	
	private String writerId;
	
	private String name;
	
	public Answerer(SnsType snsType, String writerId, String name) {
		this.snsType = snsType;
		this.writerId = writerId;
		this.name = name;
	}
	
	public SnsType getSnsType() {
		return snsType;
	}
	
	public String getWriterId() {
		return writerId;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Answerer [snsType=" + snsType + ", writerId=" + writerId + ", name=" + name + "]";
	}
}
