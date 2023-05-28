volatile int g1;
volatile int g2;

void svp_simple_020_001_main() {
      g1=1;
    if(g2==2) 
    {
        g2=2; 
    }
        g1=g2; 
}

void svp_simple_020_001_isr_1() {
    if(g1==0){
        g2=2;
    }else{
        g2=3;
    }
}
