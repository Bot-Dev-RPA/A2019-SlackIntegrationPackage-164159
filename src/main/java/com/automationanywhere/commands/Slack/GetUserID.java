package com.automationanywhere.commands.Slack;

import Utils.HTTPRequest;
import com.automationanywhere.botcommand.data.Value;
import static com.automationanywhere.commandsdk.model.AttributeType.*;
import static com.automationanywhere.commandsdk.model.DataType.STRING;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.commandsdk.model.AttributeType;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.automationanywhere.commandsdk.model.DataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be displayable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "Returns User ID", label = "Get User ID",
        node_label = "Retrieve user ID for {{user}} in session {{sessionName}}", description = "Returns Slack User ID based on name", icon = "SLACK.svg",
        comment = true ,  text_color = "#7B848B" , background_color =  "#99b3c7",
        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "Assign output to String variable", return_type = STRING, return_required = true)

public class GetUserID {
    @Sessions
    private Map<String, Object> sessions;

    private static final Messages MESSAGES = MessagesFactory.getMessages("com.automationanywhere.botcommand.demo.messages");

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private com.automationanywhere.bot.service.GlobalSessionContext globalSessionContext;

    public void setGlobalSessionContext(com.automationanywhere.bot.service.GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    //Identify the entry point for the action. Returns a Value<String> because the return type is String.
    @Execute
    public StringValue action(
            @Idx(index = "1", type = TEXT) @Pkg(label = "Session name", default_value_type = STRING,  default_value = "Default") @NotEmpty String sessionName,
            @Idx(index = "2", type = AttributeType.TEXT) @Pkg(label = "User Name", description = "e.g. johnsmith - enter the exact username as it appears in Slack") @NotEmpty String user
    ) throws IOException, ParseException {
        if ("".equals(user.trim())) {
            throw new BotCommandException(MESSAGES.getString("emptyInputString", "channel"));
        }

        if (!this.sessions.containsKey(sessionName)){
            throw new BotCommandException(MESSAGES.getString("incorrectSession",sessionName));
        }
        //Retrieve APIKey String that is passed as Session Object
        String token = (String) this.sessions.get(sessionName);
        String id = null;
        String url = "https://slack.com/api/users.list";
        String response = HTTPRequest.Request(url, "GET", token);
        //Parse JSON response to get result of request
        Object obj = new JSONParser().parse(response);
        JSONObject jsonObj = (JSONObject) obj;
        String result = jsonObj.get("ok").toString();
        if(result.equals("true")){
            JSONArray channels = (JSONArray) jsonObj.get("members");
            for (int i=0; i<channels.size(); i++){
                JSONObject userInfo = (JSONObject) channels.get(i);
                String name = userInfo.get("name").toString();
                if(name.equals(user)){
                    id = userInfo.get("id").toString();
                    break;
                } else {id="user not found";}
            }
        } else{
            String reason = jsonObj.get("error").toString();
            id = "Action failed with error reason: "+reason;
        }

        return new StringValue(id);
    }
    public void setSessions(Map<String, Object> sessions) {
        this.sessions = sessions;
    }
}
