package dice.eu.fleximonkey;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.extl.jade.user.Job;
import com.extl.jade.user.ResourceMetadata;
import com.extl.jade.user.ServerStatus;
import com.extl.jade.user.UserAPI;
import com.extl.jade.user.UserService;

public class FCOstopVM {
	@SuppressWarnings("deprecation")
	public void stopvm(String vmuuid, String cloudusername,
			String cloudpassword, String cloudapiurl, String cloudUUID) {

		UserService service;
		Job stopServer = null;

		System.setProperty("jsse.enableSNIExtension", "false");
		URL url = null;
		try {
			url = new URL(com.extl.jade.user.UserAPI.class.getResource("."),
					cloudapiurl);
			System.out.println("GOT WSDL");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("unable to get wsdl");
		}

		// Get the UserAPI
		UserAPI api = new UserAPI(url, new QName(
				"http://extility.flexiant.net", "UserAPI"));
		System.out.println("getting user API");
		// and set the service port on the service
		service = api.getUserServicePort();

		// Get the binding provider
		BindingProvider portBP = (BindingProvider) service;

		// and set the service endpoint
		portBP.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, cloudapiurl);

		// and the caller's authentication details and password
		portBP.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
				cloudusername + "/" + cloudUUID);
		portBP.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
				cloudpassword);
		// Get date time for job creation
		try {
			System.out.println("in date section");
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			DatatypeFactory datatypeFactory = null;
			datatypeFactory = DatatypeFactory.newInstance();
			XMLGregorianCalendar now = datatypeFactory
					.newXMLGregorianCalendar(gregorianCalendar);

			Date date = new Date();
			datatypeFactory = null;
			datatypeFactory = DatatypeFactory.newInstance();

			now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
			@SuppressWarnings("deprecation")
			int mins = date.getMinutes();
			int sec = date.getSeconds();
			int hours = date.getHours();
			sec += 10;
			if (sec >= 60) {
				sec -= 60;
				mins += 1;
			}
			if (mins == 60) {
				mins = 59;
			}

			now.setTime(hours, mins, sec);

			// Send stop command
			// Can also be scheduled for time in future.
			System.out.println("Sending stop command for:  " + vmuuid);
			stopServer = service.changeServerStatus(vmuuid,
					ServerStatus.STOPPED, true, new ResourceMetadata(), now);
			// waits till job completes or fails
			service.waitForJob(stopServer.getResourceUUID(), false);

			System.out.println("Server stopped:  " + vmuuid);

		} catch (Exception e) {
			System.out
					.println("Unable to send stop job or server is already stopped");
			// Perhaps update to check state currently?
			// e.printStackTrace();

		}
	}

}