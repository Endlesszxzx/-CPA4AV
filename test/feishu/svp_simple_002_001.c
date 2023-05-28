
volatile int g1;
volatile int g2;

void svp_simple_002_001_main(){
    g2=2;
    int r1=g1;
    g2=r1;
    r1=g1;
}

void svp_simple_002_001_isr_1() {
    int r2=g2;
    g1=4;
}