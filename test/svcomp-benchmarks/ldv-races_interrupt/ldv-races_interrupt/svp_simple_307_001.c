// #include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define offsetof(TYPE, MEMBER) ((unsigned long) &((TYPE *)0)->MEMBER)

#define container_of(ptr, type, member) ({                      	const typeof( ((type *)0)->member ) *__mptr = (ptr);    	(type *)( (char *)__mptr - offsetof(type,member) );})

extern void abort(void);
#include <assert.h>
void reach_error() { assert(0); }
extern void __VERIFIER_atomic_begin(void);
extern void __VERIFIER_atomic_end(void);
int __VERIFIER_nondet_int(void);
void ldv_assert(int expression) { if (!expression) { ERROR: {reach_error();abort();}}; return; }

// pthread_t t1,t2;

struct device {
	//struct device_private   *p;
};

struct A {
	int a;
	int b;
};

struct my_data {
	// pthread_mutex_t lock;
	struct device dev;
	struct A shared;
};

void svp_simple_307_001_isr_1(void *arg) {
	struct device *dev = (struct device*)arg;
	struct my_data *data;
	data = container_of(dev, struct my_data, dev);
	
	//race
	//// pthread_mutex_lock (&data->lock);
    __VERIFIER_atomic_begin();
     data->shared.a = 1;
     __VERIFIER_atomic_end();
     __VERIFIER_atomic_begin();
     int lb = data->shared.b;
     __VERIFIER_atomic_end();
     __VERIFIER_atomic_begin();
     data->shared.b = lb + 1;
     __VERIFIER_atomic_end();
    //// pthread_mutex_unlock (&data->lock);
	// return 0;
}

int my_drv_probe(struct my_data *data) {
	struct device *d = &data->dev;
	
	//init data (single thread)
	//not a race
	// pthread_mutex_init(&data->lock, NULL);
	data->shared.a = 0;
	data->shared.b = 0;
	
	int res = __VERIFIER_nondet_int();
	if(res)
		goto exit;
	//register callback
	// pthread_create(&t1, NULL, my_callback, (void *)d);
	// pthread_create(&t2, NULL, my_callback, (void *)d);
	return 0;

exit:
	// pthread_mutex_destroy(&data->lock);
	return -1;
}

void my_drv_disconnect(struct my_data *data) {
	void *status;
	// pthread_join(t1, &status);
	// pthread_join(t2, &status);
	// pthread_mutex_destroy(&data->lock);
}

int my_drv_init(void) {
	return 0;
}
 
void my_drv_cleanup(void) {
	return;
}

int svp_simple_307_001_main(void) {
	int ret = my_drv_init();
	if(ret==0) {
		int probe_ret;
		struct my_data data;
		probe_ret = my_drv_probe(&data);
		if(probe_ret==0) {
			my_drv_disconnect(&data);
			ldv_assert(data.shared.a==1);
			ldv_assert(data.shared.b==2);
		}
		my_drv_cleanup();
		data.shared.a = -1;
		data.shared.b = -1;
		ldv_assert(data.shared.a==-1);
		ldv_assert(data.shared.b==-1);
	}
	return 0;
}
