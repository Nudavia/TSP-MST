import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Union;

import java.util.Arrays;
public class Unset{
    int[] Parent;
    public Unset(int n){
        Parent=new int[n];
        Arrays.fill(Parent,-1);
    }
    public int Find(int i){
        while(Parent[i]>=0){
            i=Parent[i];
        }
        return i;
    }
    public void Merge(int i,int j){
        int pi=Find(i);
        int pj=Find(j);
        if(pi!=pj){
            if(Parent[pi]<Parent[pj]){//pi子女多,pj并入pi
                Parent[pi]+=Parent[pj];
                Parent[pj]=pi;
            }
            else{
                Parent[pj]+=Parent[pi];
                Parent[pi]=pj;
            }
        }
    }
    public void Show(){
        for(int i=0;i<Parent.length;++i){
            int pi=Find(i);
            if(i!=pi){
                System.out.println(i+" 属于 "+pi+"(有"+-Parent[pi]+"个孩子)");
            }
        }

    }
}