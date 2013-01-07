package net.slipp.domain.qna;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.slipp.domain.tag.NewTag;
import net.slipp.domain.tag.Tag;
import net.slipp.domain.user.SocialUser;
import net.slipp.repository.tag.TagRepository;
import net.slipp.service.tag.TagProcessor;
import net.slipp.support.jpa.CreatedAndUpdatedDateEntityListener;
import net.slipp.support.jpa.HasCreatedAndUpdatedDate;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDeniedException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Entity
@EntityListeners({ CreatedAndUpdatedDateEntityListener.class })
public class Question implements HasCreatedAndUpdatedDate {
	private static final int SHOW_BEST_ANSWER_RANGE = 10;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long questionId;
	
	@ManyToOne
	@org.hibernate.annotations.ForeignKey(name = "fk_question_writer")
	private SocialUser writer;
	
	@Column(name = "title", length=100, nullable = false)
	private String title;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "question_content_holder", joinColumns = @JoinColumn(name = "question_id", unique = true))
	@org.hibernate.annotations.ForeignKey(name = "fk_question_content_holder_question_id")
	@Lob
	@Column(name = "contents", nullable = false)
	private Collection<String> contentsHolder;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false, updatable = false)
	private Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date", nullable = false)
	private Date updatedDate;

	@Column(name = "answer_count", nullable = false)
	private int answerCount = 0;

	@Column(name = "show_count", nullable = false)
	private int showCount = 0;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "question_tag", joinColumns = @JoinColumn(name = "question_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@org.hibernate.annotations.ForeignKey(name = "fk_question_tag_question_id", inverseName = "fk_question_tag_tag_id")
	private Set<Tag> tags = Sets.newHashSet();
	
	@Column(name = "denormalized_tags", length=100)
	private String denormalizedTags; // 역정규화한 태그를 저장
	
	@Transient
	private Set<NewTag> newTags = Sets.newHashSet();
	
	@Transient
	private String plainTags;

	@OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
	@OrderBy("answerId ASC")
	private List<Answer> answers;
	
	@Column(name = "deleted", nullable = false)
	private boolean deleted = false;
	
	@Transient
	private boolean connected = false;
	
	@Embedded
	private SnsConnection snsConnection = new SnsConnection();
	
	public Question() {
	}
	
	public Question(Long id) {
		this.questionId = id;
	}
	
	public List<Answer> getAnswers() {
		return answers;
	}
	
	public int getAnswerCount() {
		return answerCount;
	}
	
	public Set<Tag> getTags() {
		return tags;
	}
	
	public Collection<String> getDenormalizedTags() {
		if (StringUtils.isBlank(denormalizedTags)) {
			return Sets.newHashSet();
		}
		return Arrays.asList(denormalizedTags.split(","));
	}
	
	public void setContents(String newContents) {
		if (isEmptyContentsHolder()) {
			contentsHolder = Lists.newArrayList(newContents);
		} else {
			contentsHolder.clear();
			contentsHolder.add(newContents);
		}
	}
	
	private boolean isEmptyContentsHolder() {
		return contentsHolder == null || contentsHolder.isEmpty();
	}

	public String getContents() {
		if (isEmptyContentsHolder()) {
			return "";
		}

		return Iterables.getFirst(contentsHolder, "");
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	
	public void writedBy(SocialUser user) {
		this.writer = user;
	}
	
	public boolean isWritedBy(SocialUser socialUser) {
		return writer.isSameUser(socialUser);
	}

	public SocialUser getWriter() {
		return writer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getShowCount() {
		return showCount;
	}

	public void setShowCount(int showCount) {
		this.showCount = showCount;
	}

	public String getPlainTags() {
		String displayTags = "";
		for (Tag tag : this.tags) {
			displayTags += tag.getName() + " ";
		}
		return displayTags;
	}

	public void setPlainTags(String plainTags) {
		this.plainTags = plainTags;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void delete(SocialUser loginUser) {
		if (!isWritedBy(loginUser)) {
			throw new AccessDeniedException(loginUser + " is not owner!");
		}
		this.deleted = true;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void show() {
		this.showCount += 1;
	}
	
	public void newAnswered() {
		this.answerCount += 1;
	}
	
	public void deAnswered() {
		this.answerCount -= 1;
	}
	
	public void tag(Tag tag) {
		tags.add(tag);
		this.denormalizedTags = TagProcessor.tagsToDenormalizedTags(tags);
		tag.tagged();
	}
	
	public boolean hasTag(Tag tag) {
		return tags.contains(tag);
	}
	
	public Set<NewTag> getNewTags() {
		return newTags;
	}
	
	public static Question newQuestion(SocialUser loginUser, Question questionDto, TagRepository tagRepository) {
		Question newQuestion = new Question();
		newQuestion.writer = loginUser;
		newQuestion.title = questionDto.title;
		newQuestion.contentsHolder = questionDto.contentsHolder;
		newQuestion.processTags(questionDto.plainTags, tagRepository);
		
		return newQuestion;
	}
	
	private void processTags(String plainTags, TagRepository tagRepository) {
		TagProcessor tagProcessor = new TagProcessor(tagRepository);
		tagProcessor.processTags(this.tags, plainTags);
		this.tags = tagProcessor.getTags();
		this.denormalizedTags = tagProcessor.getDenormalizedTags();
		this.newTags = tagProcessor.getNewTags();
	}
	
	public void update(SocialUser loginUser, Question questionDto, TagRepository tagRepository) {
		if (!isWritedBy(loginUser)) {
			throw new AccessDeniedException(loginUser + " is not owner!");
		}
		
		this.title = questionDto.title;
		this.contentsHolder = questionDto.contentsHolder;
		this.processTags(questionDto.plainTags, tagRepository);
	}
	
	public Set<SocialUser> findNotificationUser(SocialUser loginUser) {
		Answers newAnswers = new Answers(this.answers);
		Set<SocialUser> notifierUsers = newAnswers.findFacebookAnswerers();
		notifierUsers.add(this.writer);
		return Sets.difference(notifierUsers, Sets.newHashSet(loginUser));
	}
	
	public SnsConnection connected(String postId) {
		this.snsConnection = new SnsConnection(SnsType.valueOf(writer.getProviderId()), postId); 
		return this.snsConnection;
	}
	
	/**
	 * 베스트 댓글 하나를 반환한다.
	 * 
	 * @return
	 */
	public Answer getBestAnswer() {
		List<Answer> sortAnswers = null;
		if( getAnswers() != null && getAnswers().size() > SHOW_BEST_ANSWER_RANGE ){
			sortAnswers = Lists.newArrayList();
			sortAnswers.addAll(getAnswers());
			Collections.sort(sortAnswers);
			return sortAnswers.get(0);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Question [questionId=" + questionId + ", writer=" + writer + ", title=" + title + ", contentsHolder="
				+ contentsHolder + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + ", answerCount="
				+ answerCount + ", showCount=" + showCount + ", tags=" + tags + ", plainTags=" + plainTags
				+ ", answers=" + answers + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + answerCount;
		result = prime * result + ((answers == null) ? 0 : answers.hashCode());
		result = prime * result + ((contentsHolder == null) ? 0 : contentsHolder.hashCode());
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + ((plainTags == null) ? 0 : plainTags.hashCode());
		result = prime * result + ((questionId == null) ? 0 : questionId.hashCode());
		result = prime * result + showCount;
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
		result = prime * result + ((writer == null) ? 0 : writer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (answerCount != other.answerCount)
			return false;
		if (answers == null) {
			if (other.answers != null)
				return false;
		} else if (!answers.equals(other.answers))
			return false;
		if (contentsHolder == null) {
			if (other.contentsHolder != null)
				return false;
		} else if (!contentsHolder.equals(other.contentsHolder))
			return false;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (plainTags == null) {
			if (other.plainTags != null)
				return false;
		} else if (!plainTags.equals(other.plainTags))
			return false;
		if (questionId == null) {
			if (other.questionId != null)
				return false;
		} else if (!questionId.equals(other.questionId))
			return false;
		if (showCount != other.showCount)
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (updatedDate == null) {
			if (other.updatedDate != null)
				return false;
		} else if (!updatedDate.equals(other.updatedDate))
			return false;
		if (writer == null) {
			if (other.writer != null)
				return false;
		} else if (!writer.equals(other.writer))
			return false;
		return true;
	}
}
