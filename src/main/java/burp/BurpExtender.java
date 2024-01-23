package burp;

import burp.ui.CustomTab;
import burp.ui.CustomTableModel;
import burp.utils.HelperPlus;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author youan
 * @date 2022-10-19 22:32
 * @project symmetric_encryption burp源码打包，直接右侧maven -> package 就行. 不需要打包插件.如果是springboot 工具就需要
 * @company 聚散安全
 * @description: 是一个愿意接纳小白，不断自我革旧留精的学习之地。
 *
 */

public class BurpExtender implements IBurpExtender, IHttpListener, IContextMenuFactory {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    public CustomTab customTab;
    // 初始化UI组件
    private JTable table;
    private CustomTableModel customTableModel;
    public HelperPlus helperPlus;
    //public JMenuItem menuItem;
    public IHttpRequestResponse[] selectedMessages;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.helperPlus=new HelperPlus(helpers);

        callbacks.setExtensionName("jX-CSRF ");

        // Create a custom tab 使用 SwingUtilities.invokeLater 来确保UI更新在事件分发线程中进行，以避免并发问题
        SwingUtilities.invokeLater(() -> {
            customTableModel = new CustomTableModel();
            table = new JTable(customTableModel);

            // 创建自定义Tab页面
            customTab = new CustomTab(callbacks,helpers);
            callbacks.customizeUiComponent(customTab);
            callbacks.addSuiteTab(customTab);
        });

        callbacks.registerHttpListener(this);
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {

        //响应处理
        if(!messageIsRequest){
            int length1 = messageInfo.getResponse().length;
            short statusCode = helpers.analyzeResponse(messageInfo.getResponse()).getStatusCode();
        }

    }
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        callbacks.printOutput("createMenuItems 菜单获取的请求信息： "+helpers.bytesToString(invocation.getSelectedMessages()[0].getRequest()));
        // 获取被选中的请求记录
        IHttpRequestResponse[] selectedMessages = invocation.getSelectedMessages();
        //selectedMessages = selectedMessages;
        List<JMenuItem> menuItems = new ArrayList<>();

        if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY ||
                invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TREE ||
                invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {

            JMenuItem menuItem = new JMenuItem("Send to JX-CSRF");
            menuItem.addActionListener(e -> executeRequestsInParallel(invocation));

            menuItems.add(menuItem);
        }

        return menuItems;
    }


    private void executeRequestsInParallel(IContextMenuInvocation invocation) {
        IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages(); //= invocation.getSelectedMessages();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        // 预估要发3个请求
        CountDownLatch latch = new CountDownLatch(3);
        Future<Object> firstResultFuture = null;
        for (IHttpRequestResponse messageInfo : selectedItems) {

            IHttpService httpService = messageInfo.getHttpService();
            IRequestInfo requestInfo = helpers.analyzeRequest(httpService,messageInfo.getRequest());

            String referer = helperPlus.getHeaderValueOf(true, messageInfo, "referer");
            String method = requestInfo.getMethod();
            URL url = requestInfo.getUrl();

            List<byte[]> list = new ArrayList<byte[]>();
            // 原请求响应
            //int length = sendPOC(messageInfo, httpService, method, url,0);//原响应长度
            int length =0;
            list.add(messageInfo.getRequest());
            // 1. 空referer绕过
            byte[] request1 = helperPlus.addOrUpdateHeader(true, messageInfo.getRequest(), "Referer", "");
            list.add(request1);
            // 2.包含referer
            byte[] request2 = helperPlus.addOrUpdateHeader(true, messageInfo.getRequest(), "Referer", "https://test.com/" + referer);
            list.add(request2);

            for(int i=0;i<list.size();i++){
                final int index = i;
                byte[] request = list.get(i);
                callbacks.printOutput("list循环发送时的message"+i+": "+new String(request));
                Future<Object> future = executor.submit(() -> {
                    Object result = sendPOC(request, httpService, method, url);
                    latch.countDown();
                    return result;
                });
                // 记录第一个任务的Future以便后续获取结果
                if (firstResultFuture == null) {
                    firstResultFuture = future;
                }
//                executor.submit(() -> {
//                    sendPOC(request, httpService, method, url);
//                    latch.countDown();
//                });
            }
            // 等待第一个任务完成并获取结果
            try {
                Object firstResult = firstResultFuture.get();
                callbacks.printOutput("第一个请求的结果: " + firstResult);
            } catch (InterruptedException | ExecutionException e) {
                callbacks.printError("获取第一个请求结果时发生错误: " + e.getMessage());
            }
            // 确保所有任务都完成
            try {
                latch.await();
            } catch (InterruptedException e) {
                callbacks.printError("等待任务完成时发生中断: " + e.getMessage());
            }
            // 关闭线程池
            executor.shutdown();
        }

        try {
            latch.await(); // 等待所有任务完成
        } catch (InterruptedException e) {
            callbacks.printError("Interrupted while waiting for requests to finish: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    public int sendPOC(byte[] request,IHttpService httpService,String method,URL url){
        System.out.println("URL:"+url);
        System.out.println("URL.toString: "+url.toString());
        IHttpRequestResponse iHttpRequestResponse1 = callbacks.makeHttpRequest(httpService, request);
        int length = iHttpRequestResponse1.getResponse().length;
        short statusCode = helpers.analyzeResponse(iHttpRequestResponse1.getResponse()).getStatusCode();
        customTab.addRecord(method, url.toString(),statusCode,length,iHttpRequestResponse1);
        return length;
    }

}
