import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject("<issue key>");
CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("<Custom field name>");
Object valueOriginal = issue.getCustomFieldValue(customField);

FieldLayoutManager fieldLayoytManager = ComponentAccessor.getComponent(FieldLayoutManager.class);
FieldLayoutItem fieldLayoutItem = fieldLayoytManager.getFieldLayout(issue).getFieldLayoutItem(customField.getId());
String fieldHtml = String.valueOf(customField.getCustomFieldType().getDescriptor().getViewHtml(customField, issue.getCustomFieldValue(customField), issue, fieldLayoutItem)).trim();

return String.format("issue = \"%s\""+
                     "<br>original value = \"%s\""+
                     "<br>view value = \"%s\"",
                     issue.getKey(),
                     valueOriginal,
                     fieldHtml);
