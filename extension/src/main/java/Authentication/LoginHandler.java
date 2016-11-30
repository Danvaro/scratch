package Authentication;

import Database.DatabaseConnection;
import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class LoginHandler extends BaseServerEventHandler {

    // TODO: Create method for throwing SFSExceptions
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {

        SFSObject sfso = (SFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);
        if (sfso.getBool("guest")) {
            return;
        }

        String playerusername = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
        ensurePlayerUsernameNotEmpty(playerusername);

        String playerpassword = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
        ensurePlayerPasswordNotEmpty(playerpassword);

        Document user;
        user = findUserInDatabase(playerusername);

        String password;
        password = (String) user.get("password");

        ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);
        checkUserPasswordFromDatabase(session, password, playerpassword);
    }

    private void ensurePlayerUsernameNotEmpty(String playerusername) throws SFSException {
        if (playerusername.equals("")) {
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
            data.addParameter(playerusername);
            throw new SFSLoginException("No username specified.", data);
        }
    }

    private void ensurePlayerPasswordNotEmpty(String playerpassword) throws SFSException {
        if (playerpassword.equals("")) {
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            data.addParameter(playerpassword);
            throw new SFSLoginException("No password specified.", data);
        }
    }

    private Document findUserInDatabase(String playerusername) throws SFSException {
        final Document user = DatabaseConnection.users.find(eq("username", playerusername)).first();
        if (user == null) {
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
            data.addParameter(playerusername);
            throw new SFSLoginException("Username not found in database.", data);
        }
        return user;
    }

    private void checkUserPasswordFromDatabase(ISession session, String password, String playerpassword) throws SFSException {
        if (!getApi().checkSecurePassword(session, password, playerpassword)) {
            SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            data.addParameter(playerpassword);
            throw new SFSLoginException("Password incorrect", data);
        }
    }
}



