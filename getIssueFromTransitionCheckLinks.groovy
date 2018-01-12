import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkListResult;
import com.google.common.base.Strings;
import groovy.lang.MissingPropertyException;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.opensymphony.workflow.InvalidInputException;

// получение текущего запроса и тестового
// Issue issue = ImanGetIssue.getIssue(this,"DEVSUB-8")
public class ImanGetIssue{
    // используется для информирования при тестировании и проверки
    public static String info = "";
    static String getInfo(){
        return info;
    }
    static String getInfoHTML(){
        return info.replace("\n","<br>");
    }
    static String addInfo(String info){
        this.info += info;
        return this.info;
    }
    static String addInfoLine(String info){
        this.info += String.format("\n%s",info);
        return this.info;
    }
    static String setInfo(String info){
        this.info = info;
        return this.info;
    }
    // получение идентификатора задачи для действия WF или при тестировании/проверки в консоли
    // IssueTestKey - указывается номер запроса, на котором требуется проверить
    // если вызывается в действии Workflow, то IssueTestKey игнорируется
    static public Issue getIssue(Object script, String IssueTestKey){
        if( script == null ) return null;
        
        boolean hasTransientVars = false;
        try{
            hasTransientVars = script.getAt("transientVars") ? true: false;
        } catch(groovy.lang.MissingPropertyException e){}
        
        Issue issue = null;
        try{
            issue = (Issue)(script.getAt("issue"));
        } catch(groovy.lang.MissingPropertyException e){
            if(hasTransientVars || Strings.isNullOrEmpty(IssueTestKey)) return null;
            
            issue = ComponentAccessor.getIssueManager().getIssueObject(IssueTestKey)
            script.putAt("issue", issue);     
        }
        
        return issue;
    }
}

// проверка наличия связей с внешними источниками, такими как Confluence, Bitbucket и т.д.
// без вывода ошибки
// new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink() - для любых внешних ссылок
// new ImanIssueGetRemoteLink(issueCheckLink).checkHasRemoteLink() - для внешних ссылок Web
// new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence") - без учета типом отношения (с какой стороны привязано от JIRA или от Confluence)
// new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence","Wiki Page") - с учетом типом отношения (с какой стороны привязано от JIRA или от Confluence)
// new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence",["Wiki Page","mentioned in"]) - с учетом типом отношения (с какой стороны привязано от JIRA или от Confluence)
// с выводом ошибки
// new ImanIssueGetRemoteLink(issueCheckLink).validateLink("com.atlassian.confluence")
public class ImanIssueGetRemoteLink{
    private Issue issue;
    
    public Issue getIssue(){
        return issue;
    }
    public Issue setIssue(Issue issue){
        this.issue = issue;
        return this.issue;
    }
    public ImanIssueGetRemoteLink(){
        issue = null;
    }
    public ImanIssueGetRemoteLink(Issue issue){
        setIssue(issue);
    }
    public RemoteIssueLinkService.RemoteIssueLinkListResult getRemoteLinks(Issue issue){
        setIssue(issue);
        return getRemoteLinks();
    }
    public RemoteIssueLinkService.RemoteIssueLinkListResult getRemoteLinks(){
        
		ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        RemoteIssueLinkService remoteIssueLinkService = ComponentAccessor.getComponentOfType(RemoteIssueLinkService.class);
        
        return remoteIssueLinkService.getRemoteIssueLinksForIssue(loggedInUser, getIssue());
    }
    
    public boolean checkHasLink(String applicationType, String relationship, Issue issue){
        setIssue(issue);
        return checkHasLink(applicationType, relationship);
    }
    public boolean checkHasLink(String applicationType, String relationship){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        RemoteIssueLink link = remoteLinks.getRemoteIssueLinks().find{ RemoteIssueLink link ->
            return link.getApplicationType().equals(applicationType) && link.getRelationship().equals(relationship);
        }
        return link == null ? false : true;
    }
    
    public boolean checkHasLink(String applicationType, List<String> listRelationship, Issue issue){
        setIssue(issue);
        return checkHasLink(applicationType, listRelationship);
    }
    public boolean checkHasLink(String applicationType, List<String> listRelationship){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        RemoteIssueLink link = remoteLinks.getRemoteIssueLinks().find{ RemoteIssueLink link ->
            if( link.getApplicationType().equals(applicationType) ){
                String relationship = listRelationship.find{ item->
                    return link.getRelationship().equals(item);
                }
                return relationship == null ? false : true;
            }
            return false;
        }
        return link == null ? false : true;
    }
    public boolean checkHasLink(String applicationType){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        RemoteIssueLink link = remoteLinks.getRemoteIssueLinks().find{ RemoteIssueLink link ->
            return link.getApplicationType().equals(applicationType);
        }
        return link == null ? false : true;
    }
    public boolean checkHasLink(){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        return remoteLinks.getRemoteIssueLinks().size() > 0 ? true : false;
    }
    public boolean checkHasAppLink(){
        return checkHasAppLink(false);
    }
    public boolean checkHasAppLink(boolean isOnly){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        RemoteIssueLink link = remoteLinks.getRemoteIssueLinks().find{ RemoteIssueLink item ->
            return item.getApplicationType() != null;
        }
        if( link != null ){
            if(isOnly){
                link = remoteLinks.getRemoteIssueLinks().find{ item ->
                    return item.getApplicationType() == null;
                }
                return link == null ? true : false;
            } else {
                return true;
            }
        }
        return false;
    }
    public boolean checkHasRemoteLink(){
        return checkHasRemoteLink(false);
    }
    public boolean checkHasRemoteLink(boolean isOnly){
        RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = this.getRemoteLinks();
        RemoteIssueLink link = remoteLinks.getRemoteIssueLinks().find{ RemoteIssueLink item ->
            return item.getApplicationType() == null && item.getRelationship() == null;
        }
        if( link != null ){
            if(isOnly){
                link = remoteLinks.getRemoteIssueLinks().find{ item ->
                    return item.getApplicationType() != null;
                }
                return link == null ? true : false;
            } else {
                return true;
            }
        }
        return false;
    }
    
    public validateLink(){
        if( !checkHasLink() ){
            throw new InvalidInputException( String.format(//"Требуется добавить внешние ссылки для запроса <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется добавить внешние ссылки. "
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }
    public validateAppLink(boolean isOnly){
        if( !checkHasAppLink(isOnly) ){
            throw new InvalidInputException( String.format(//"Требуется проверить наличие ссылок на внешние приложения по запросу <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется проверить наличие ссылок на внешние приложения."
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }
    public validateRemoteLink(boolean isOnly){
        if( !checkHasRemoteLink(isOnly) ){
            throw new InvalidInputException( String.format(//"Требуется проверить наличие ссылок на внешние ссылки по запросу <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется проверить наличие ссылок на внешние ссылки."
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }
    public validateLink(String applicationType){
        if( !checkHasLink(applicationType) ){
            throw new InvalidInputException( String.format(//"Требуется проверить наличие ссылок на внешние приложения по запросу <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется проверить наличие ссылок на внешние приложения."
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }
    public validateLink(String applicationType, String relationship){
        if( !checkHasLink(applicationType, relationship) ){
            throw new InvalidInputException( String.format(//"Требуется проверить наличие ссылок на внешние приложения по запросу <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется проверить наличие ссылок на внешние приложения по запросу. "
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }    
    public validateLink(String applicationType, List<String> listRelationship){
        if( !checkHasLink(applicationType, listRelationship) ){
            throw new InvalidInputException( String.format(//"Требуется проверить наличие ссылок на внешние приложения по запросу <a href=\"/browse/%s\">. "+
                                                           //"Для добавления внешней ссылки требуется открыть <a href=\"/secure/LinkJiraIssue!default.jspa?id=%d\">связи</a>.",
                                                           "Требуется проверить наличие ссылок на внешние приложения"
                                                           //issue.getKey(),
                                                           //issue.getId()
                                                          ));
        }
    }
}

Issue issueCheckLink = ImanGetIssue.getIssue(this,"DEVSUB-8");
new ImanIssueGetRemoteLink(issueCheckLink).validateAppLink(true);

/*
RemoteIssueLinkService.RemoteIssueLinkListResult remoteLinks = new ImanIssueGetRemoteLink(issueCheckLink).getRemoteLinks();

ImanGetIssue.setInfo( "<table class=\"aui aui-table\"> "+
                      "<thead><tr><th>Parameter</th><th>Value</th></tr></thead> "+
                      "<tbody> " );
ImanGetIssue.addInfoLine( String.format("<tr> <td> issue </td> <td> <a href=\"/browse/%s\">%s</a> </td> </tr>", issueCheckLink.getKey(), issueCheckLink.getKey()) );
ImanGetIssue.addInfoLine( String.format("<tr> <td> remoteLinks </td> <td> %s </td> </tr>", remoteLinks) );
ImanGetIssue.addInfoLine( "" );
remoteLinks.getRemoteIssueLinks().each{ RemoteIssueLink link ->
    if( link.getApplicationType().equals("com.atlassian.confluence") || 1==1 ){
        ImanGetIssue.addInfoLine( String.format("<tr> <td> link </td> <td> %s</td> </tr> "+
                                                "<tr> <td> Application Type </td> <td> %s</td> </tr> "+
                                                "<tr> <td> Title </td> <td> %s</td> </tr> "+
                                                "<tr> <td> URL </td> <td> %s</td> </tr> "+
                                                "<tr> <td> Relationship </td> <td> %s </td> </tr>",
                                                link,
                                                link.getApplicationType(),
                                                link.getTitle(),
                                                link.getUrl(),
                                                link.getRelationship()
                                               ) );
    }
}
ImanGetIssue.addInfoLine( "<tr><td colspan=2></td></tr>" );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS LINKS \"com.atlassian.confluence\",\"Wiki Page\" </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence","Wiki Page")) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS LINKS \"com.atlassian.confluence\",[\"Wiki Page1\",\"mentioned in1\"] </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence",["Wiki Page1","mentioned in1"])) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS LINKS \"com.atlassian.confluence\" </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink("com.atlassian.confluence")) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS ANY LINKS </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasLink()) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> ONLY APP LINKS </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasAppLink(true)) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS APP LINKS </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasAppLink(false)) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> ONLY WEB LINKS </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasRemoteLink(true)) );
ImanGetIssue.addInfoLine( String.format(" <tr> <td> HAS WEB LINKS </td> <td> %s </td> </tr>", new ImanIssueGetRemoteLink(issueCheckLink).checkHasRemoteLink(false)) );

ImanGetIssue.addInfoLine( "</tbody> </table>" );
//new ImanIssueGetRemoteLink(issueCheckLink).validateAppLink(true);
*/
return ImanGetIssue.getInfoHTML();