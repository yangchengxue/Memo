package com.yxy.memo.interfaces;

/**
  *
  * @Package:        com.yxy.memo.interfaces
  * @InterfaceName:  callback_MainActivity_Pop
  * @CreateDate:     2019/12/19 15:28
  * @Description:    回调接口，用于与MainActivity通信
 */
public interface callback_MainActivity_Pop {
    void getTitleText(String result); //将Title名称从SpinnerPopuwindow传给MainActivity

    void getNotesSum(int result); //将笔记总数从ContentEntryAdapter传给MainActivity
}
