package com.keunsy.syncdata.subscriber.threads.write;

import com.keunsy.syncdata.common.control.Stoppable;
import com.keunsy.syncdata.common.utils.CommonLogFactory;
import com.keunsy.syncdata.subscriber.dataCenter.QueueCenter;
import com.keunsy.syncdata.subscriber.dataCenter.RedisCfgCenter;
import com.keunsy.syncdata.subscriber.entity.TdInfo;
import com.keunsy.syncdata.subscriber.service.BasicService;
import com.keunsy.syncdata.subscriber.service.TdInfoService;

import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 
 *     
 * 项目名称：syncDataPublisher    
 * 类名称：TdInfoWriteThread    
 * 类描述：    
 * 创建人：chenrong1    
 * 创建时间：2015-9-28 下午2:08:30    
 * 修改人：chenrong1    
 * 修改时间：2015-9-28 下午2:08:30    
 * 修改备注：    
 * @version     
 *
 */
@Service
public class TdInfoWriteThread extends Thread implements Stoppable {

    @Resource
    private BasicService basicService;
    @Resource
    private TdInfoService tdInfoService;

    private final Log log = CommonLogFactory.getLog("infoLog");

    private boolean isRunnable = false;

    public TdInfoWriteThread() {
        this.isRunnable = true;
    }

    @Override
    public void run() {
        log.info(" Start!");
        long time = System.currentTimeMillis();
        while (isRunnable) {
            try {
                // 队列获取数据
                TdInfo tdInfo = basicService.getInfoFromQueue(QueueCenter.getTdInfoQueue());
                if (tdInfo != null) {
                    int result = tdInfoService.uptToDataSource(tdInfo, RedisCfgCenter.KEY_POST_TDINFO);
                    if (result < 1) {
                        log.error("Execute Sql Fail");
                        this.noExceptionSleep(500);
                    } else {
                        log.info("Execute Sql Success!");
                    }
                } else {
                    this.noExceptionSleep(1000);//队列为空，休眠
                }
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
                this.noExceptionSleep(500);
            }
        }
        log.info("  Run Time = " + (System.currentTimeMillis() - time));
    }

    @Override
    public boolean doStop() {
        this.isRunnable = false;
        this.interrupt();
        log.info(" Stop!");
        return true;
    }

    public void noExceptionSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
