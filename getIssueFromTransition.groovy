import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.Issue;
import com.google.common.base.Strings;
import groovy.lang.MissingPropertyException;

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

Issue issueCheckLink = ImanGetIssue.getIssue(this,"DEVSUB-8");
