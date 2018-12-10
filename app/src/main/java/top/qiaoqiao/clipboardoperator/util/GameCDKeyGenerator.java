package top.qiaoqiao.clipboardoperator.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuqinfa(dazhi)
 * @date 2018-10-15
 */
public class GameCDKeyGenerator {
    //01170000017zvjlo
    //0118000001D5KUXM
    private static String code1 = "01170000017";//"0118000001";
    private static String code1p1 = "";//"5";
    private static String[] code2 = {"z","v","j","l","o"};//{"d","k","u","x","m"};
    private static int wordLength = code2.length;
    private static int arrayCode = 32;//2的5次方
    private static int spaceCount = 16;
    private static int[] usedCode = {};
    /**
     * 生成编号（屠龙世界-小游戏-CDKey）
     * @return
     */
    public static List<String> generateCode(){
        List<String> resultList = new ArrayList<>();
        for(int i = 0; i<arrayCode;i++){
            if(ArrayUtils.contains(usedCode,i)){
                continue;
            }
            String binaryStr = Integer.toBinaryString(i);
            byte results[] = binaryStr.getBytes();
            if(results.length<wordLength){
                byte[] tempArr = new byte[wordLength];
                for (int j = 0; j < wordLength; j++) {
                    if(j>=wordLength - results.length){
                        tempArr[j] = results[j-wordLength+results.length];
                    }else{
                        tempArr[j] = 48;
                    }
                }
                results = tempArr;
            }
            StringBuilder sb = new StringBuilder(code1);
            for (int j = 0; j < results.length; j++) {
//                System.out.print((results[j]-48));//48-0;49-1
                if(results[j]==49){
                    sb.append(code2[j].toUpperCase());
                }else{
                    sb.append(code2[j].toLowerCase());
                }
                if(j==0){//将第一个英文字母后的数字补充上
                    sb.append(code1p1);
                }
            }

            resultList.add(sb.toString());
            //添加带空格的
            for (int j = 0; j < spaceCount; j++) {
                sb.append(" ");
                resultList.add(sb.toString());
            }

        }
        return resultList;
    }
    public static final void main(String[] args){
        List<String> list = generateCode();
        for (String s :  list) {
            System.out.println("\""+s+"\",");
        }
    }
}
