package org.iotacontrolcenter.ui.controller.worker;


import org.iotacontrolcenter.dto.ActionResponse;
import org.iotacontrolcenter.dto.IccrPropertyListDto;
import org.iotacontrolcenter.ui.app.Constants;
import org.iotacontrolcenter.ui.controller.ServerController;
import org.iotacontrolcenter.ui.panel.ServerPanel;
import org.iotacontrolcenter.ui.properties.locale.Localizer;
import org.iotacontrolcenter.ui.proxy.BadResponseException;
import org.iotacontrolcenter.ui.proxy.ServerProxy;
import org.iotacontrolcenter.ui.util.UiUtil;

public class RestartIccrWorker extends ActionResponseAbstractApiWorker {

    public RestartIccrWorker(Localizer localizer, ServerPanel serverPanel,
                           ServerProxy proxy, ServerController ctlr, String action,
                           IccrPropertyListDto actionProps) {
        super(localizer, serverPanel, proxy, ctlr, action, actionProps);
    }

    @Override
    protected Object doIt() throws BadResponseException {
        return proxy.doIccrAction(action, actionProps);
    }

    @Override
    protected void done() {
        //System.out.println(ctlr.name + " ICCR " + action + " done");

        Object rval = null;
        try {
            rval = get();
        } catch (Exception e) {
            System.out.println(ctlr.name + " " + action + " done: exception from get: ");
            e.printStackTrace();
        }

        if (rval == null) {
            System.out.println(ctlr.name + " " + action + " done: return value is empty");
            return;
        }

        BadResponseException bre = null;
        Exception exc = null;
        ActionResponse resp = null;

        if (rval instanceof BadResponseException) {
            bre = (BadResponseException) rval;
        } else if (rval instanceof Exception) {
            exc = (Exception) rval;
        } else if (rval instanceof ActionResponse) {
            resp = (ActionResponse) rval;
        } else {
            System.out.println(ctlr.name + " " + action + " unexpected return type: " + rval.getClass().getCanonicalName());
            return;
        }

        ctlr.setConnected(false);

        if (bre != null) {
            System.out.println(ctlr.name + " " + action + " bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());

            serverPanel.addConsoleLogLine(localizer.getLocalText(bre.errMsgkey));
            serverPanel.addConsoleLogLine(bre.resp.getMsg());

            UiUtil.showErrorDialog(ctlr.name + " " + localizer.getLocalText(bre.errMsgkey),
                    bre.resp.getMsg());
        }
        else if (exc != null) {
            System.out.println(ctlr.name + " " + action + " exception from proxy: ");
            exc.printStackTrace();

            serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogIotaNotStarted"));
            serverPanel.addConsoleLogLine(exc.getLocalizedMessage());

            UiUtil.showErrorDialog(ctlr.name + " " + localizer.getLocalText("restartIccrActionError"),
                    localizer.getLocalText("iccrApiException") + ": " + exc.getLocalizedMessage());

        }
        else if (resp != null) {
            String actionStatus = getActionStatusFromResponse(Constants.ACTION_RESPONSE_ICCR_RESTART, resp);
            if(actionStatus == null || actionStatus.isEmpty() ||
                    !actionStatus.equals(Constants.ACTION_STATUS_TRUE)) {
                serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogIccrNotRestarted"));
            }
            else {
                ctlr.setIotaActive(true);
                serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogIccrIsRestarted"));
                serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogReconnecting"));
            }
        } else {
            System.out.println(ctlr.name + " " + action + " done: unexpected place...");

        }
    }
}

