package com.example.mywifimanager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mywifimanager.base.CommonAdapter;
import com.example.mywifimanager.base.MScanWifi;
import com.example.mywifimanager.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyWifiActivity extends AppCompatActivity {
    private final String TAG = "MyWifiActivity";
    public WifiManager mWifiManager;
    public List<MScanWifi> mScanWifiList = new ArrayList<>();
    public List<ScanResult> mWifiList;
    public List<WifiConfiguration> mWifiConfiguration;
    public Context context;
    public Switch wifiSwitch;
    public ListView listView;
    private IntentFilter mFilter;
    private LinkWifi linkWifi;
    public LayoutInflater Inflater;
    private LinearLayout layout;
    private CommonAdapter<MScanWifi> adapter;

    Runnable run;
    Handler myHandler;

    ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywifi);
        context = this;
        Inflater = LayoutInflater.from(context);
        mWifiManager = (WifiManager) context.getSystemService(Service.WIFI_SERVICE);
//        mScanner = new Scanner(this);
        linkWifi = new LinkWifi(context);
        run = new Runnable() {
            @Override
            public void run() {
                myHandler.sendEmptyMessage(0);
                myHandler.postDelayed(this, 10000);
            }
        };
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        mWifiManager.startScan();
                        break;
                }
            }
        };
        initView();
        registerListener();
        registerBroadcast();
        test();

        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void initView() {
        wifiSwitch = findViewById(R.id.switch_status);
        layout = findViewById(R.id.ListView_LinearLayout);
        listView = findViewById(R.id.mlistview);

        adapter = new CommonAdapter<MScanWifi>(context,
                mScanWifiList,R.layout.item_wifi) {
            @Override
            public void convert(ViewHolder helper, MScanWifi item) {
                // TODO Auto-generated method stub
                helper.setText(R.id.tv1, item.getWifiName());
                Log.i(TAG, item.getWifiName()+"是否开放"+item.getIsLock());
                if(item.getIsLock()){
                    helper.setImageResource(R.id.img_wifi_level, R.drawable.wifi_signal_lock, item.getLevel());
                } else {
                    helper.setImageResource(R.id.img_wifi_level, R.drawable.wifi_signal_open, item.getLevel());
                }
                TextView tv2 = helper.getView(R.id.tv2);
                if(item.getIsExsit()){
                    tv2 = helper.getView(R.id.tv2);
                    tv2.setText("已保存");
                    tv2.setVisibility(View.VISIBLE);
                } else {
                    tv2.setVisibility(View.GONE);
                }

            }
        };
        listView.setAdapter(adapter);
    }

    private void registerBroadcast() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    /**
     * 广播接收，监听网络
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi状态发生改变。
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			/*int wifiState = intent.getIntExtra(
					WifiManager.EXTRA_WIFI_STATE, 0);*/
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                updateWifiStateChanged(wifiState);
                Log.d(TAG, "wifiState=" + wifiState);
            } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                updateWifiList();
            }
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                //获取当前网络状态
                int networkType = intent.getIntExtra("networkType", -1);
                connChange(networkType);
            }
        }
    };

    private void registerListener() {
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mWifiManager.isWifiEnabled()) {// 当前wifi不可用
                        mWifiManager.setWifiEnabled(true);
                    }
                    mWifiManager.startScan();
                } else {
                    if (mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(false);
                    }
                }
            }
        });

        //给item添加监听事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                // 本机已经配置过的wifi
                final ScanResult wifi = mScanWifiList.get(position).scanResult;
                final WifiConfiguration wifiConfig = linkWifi.isExsits(wifi.SSID);
                if(wifiConfig != null){
                    final int netID = wifiConfig.networkId;
                    String actionStr;
                    // 如果目前连接了此网络
                    if (mWifiManager.getConnectionInfo().getNetworkId() == netID) {
                        actionStr = "断开";
                    } else {
                        actionStr = "连接";
                    }
                    android.app.AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("提示");
                    builder.setMessage("请选择你要进行的操作？");
                    builder.setPositiveButton(actionStr,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    if (mWifiManager.getConnectionInfo()
                                            .getNetworkId() == netID) {
                                        mWifiManager.disconnect();
                                    } else {

                                        linkWifi.setMaxPriority(wifiConfig);
                                        linkWifi.connectToNetID(wifiConfig.networkId);
                                    }

                                }
                            });
                    builder.setNeutralButton("忘记",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    mWifiManager.removeNetwork(netID);
                                    return;
                                }
                            });
                    builder.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    return;
                                }
                            });
                    builder.show();

                    return;

                }
                if (mScanWifiList.get(position).getIsLock()) {
                    // 有密码，提示输入密码进行连接

                    // final String encryption = capabilities;

                    LayoutInflater factory = LayoutInflater.from(context);
                    final View inputPwdView = factory.inflate(R.layout.dialog_inputpwd,
                            null);
                    new AlertDialog.Builder(context)
                            .setTitle("请输入该无线的连接密码")
                            .setMessage("无线SSID：" + wifi.SSID)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(inputPwdView)
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            EditText pwd = (EditText) inputPwdView
                                                    .findViewById(R.id.etPassWord);
                                            String wifipwd = pwd.getText().toString();

                                            // 此处加入连接wifi代码
                                            int netID = linkWifi.createWifiInfo2(
                                                    wifi, wifipwd);

                                            linkWifi.connectToNetID(netID);
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                        }
                                    }).setCancelable(false).show();

                } else {
                    // 无密码
                    new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("你选择的wifi无密码，可能不安全，确定继续连接？")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {

                                            // 此处加入连接wifi代码
                                            int netID = linkWifi.createWifiInfo2(
                                                    wifi, "");

                                            linkWifi.connectToNetID(netID);
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            return;
                                        }
                                    }).show();

                }

            }

        });
    }

    private void updateWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING://正在打开WiFi
                wifiSwitch.setEnabled(false);
                Log.i(TAG, "正在打开WiFi");
                break;
            case WifiManager.WIFI_STATE_ENABLED://WiFi已经打开
                //setSwitchChecked(true);
                wifiSwitch.setEnabled(true);
                wifiSwitch.setChecked(true);
                scanner();
                Log.i(TAG, "WiFi已经打开");
                break;
            case WifiManager.WIFI_STATE_DISABLING://正在关闭WiFi
                wifiSwitch.setEnabled(false);
                Log.i(TAG, "正在关闭WiFi");
                break;
            case WifiManager.WIFI_STATE_DISABLED://WiFi已经关闭
                wifiSwitch.setEnabled(true);
                wifiSwitch.setChecked(false);
                clearListView();
                myHandler.removeCallbacks(run);
                Log.i(TAG, "WiFi已经关闭  ");
                break;
            default:
                wifiSwitch.setEnabled(true);
                break;
        }
    }

    private void updateWifiList() {
        final int wifiState = mWifiManager.getWifiState();
        //获取WiFi列表并显示
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                //wifi处于开启状态
                mWifiList = mWifiManager.getScanResults();
                MScanWifi mScanwifi = null;
                mScanWifiList.clear();
                for(int i = 0; i < mWifiList.size(); i++){
                    Log.d(TAG, "wifi结果: " + mWifiList.get(i).SSID + " RSSI:" + mWifiList.get(i).level);
                    int level=WifiManager.calculateSignalLevel(mWifiList.get(i).level,4);
                    String mwifiName = mWifiList.get(i).SSID;
                    boolean boolean1=false;
                    if(mWifiList.get(i).capabilities.contains("WEP")||mWifiList.get(i).capabilities.contains("PSK")||
                            mWifiList.get(i).capabilities.contains("EAP")){
                        boolean1=true;
                    }else{
                        boolean1=false;
                    }
                    mScanwifi=new MScanWifi(mWifiList.get(i),mwifiName,level,boolean1);
                    if(linkWifi.isExsits(mwifiName)!=null){
                        mScanwifi.setIsExsit(true);
                    }
                    else {mScanwifi.setIsExsit(false);
                    }
                    mScanWifiList.add(mScanwifi);
                }
                //listview刷新数据
                adapter.notifyDataSetChanged();
                break;
            case WifiManager.WIFI_STATE_ENABLING:

                break;//如果WiFi处于正在打开的状态，则清除列表
        }
    }

    private void clearListView() {
        mScanWifiList.clear();
        adapter.notifyDataSetChanged();
    }

    private void scanner() {
        myHandler.postDelayed(run, 10000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(mReceiver); // 注销此广播接收器
        myHandler.removeCallbacks(run);
    }

    private void connChange(int state) {

        switch (state) {
            case -1:
                break;
            case ConnectivityManager.TYPE_MOBILE:

                break;
            case ConnectivityManager.TYPE_WIFI:
                NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (info != null && info.isConnected()) {
                    Log.i(TAG, "Wifi网络连接成功");
                    Toast.makeText(context, "Wifi网络连接成功", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Wifi网络连接断开");
                    Toast.makeText(context, "Wifi网络连接断开", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

                break;
        }
    }

    private void test() {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);

        listView.setLayoutAnimation(controller);
    }
}
