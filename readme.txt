libhttp�ǻ�������������volley�ļ򵥷�װ
��Ҫ�ӿڰ�����
1.HttpRequestManager   ����libhttp����ĳ�ʼ��
2.AbsHttpRequestProxy  ����http��������֧࣬����չ�����͹�������
3.HttpRequestProxy     http���������
3.HttpReqParam         ������http�����ע����

ʹ�÷�����
1.��ʼ��
HttpRequestManager.getInstance().init(context);

2.��������
1������ResponseModel
public class DialogBean {
   //����response���ݽṹ
    
}


2������RequestModel

@HttpReqParam(responseType = DialogBean.class, protocal = "http://192.168.30.6/hybrid/h5zip/raw/develop/app/config_dialog.json")
public class HttpGetDialogConfigRequest {
  //����request���ݽṹ	
	
}


3.��������
HttpRequestProxy.get().create(new HttpTestRequest(), new AbsHttpRequestProxy.RequestListener<DialogBean>() {
    @Override
    public void onSuccess(DialogBean response) {
	DialogBean.HomepageBean bean = response.getHomepage().get(0);
    }

    @Override
    public void onFailed(VolleyError error) {

    }
}).tag(this.toString()).cache(false).gzip(true).build().excute();


4.ȡ������
proxy.cancel();



5.����ȡ������
HttpRequestManager.getInstance().cancelAll(tag);