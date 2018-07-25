/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.includes;

public interface nec765H
{
	
	
	public static final int NEC765_DAM_DELETED_DATA = 0x0f8;
	public static final int NEC765_DAM_DATA = 0x0fb;
	
	public static interface nec765_interface
	{
		/* interrupt issued */
		public void interrupt(int state);
	
		/* dma data request */
		void dma_drq(int state,int read_write);
	};
	
	/* set nec765 dma drq output state */
	void	nec765_set_dma_drq(int state);
	/* set nec765 int output state */
	void nec765_set_int(int i);
	
	/* init nec765 interface */
	void nec765_init(nec765_interface nInterface, int version);
	/* set nec765 terminal count input state */
	abstract void nec765_set_tc_state(int i);
	/* set nec765 ready input*/
	void nec765_set_ready_state(int i);
	
        
        public static final int NEC765A =0;
        public static final int NEC765B =1;
	public static final int SMC37C78=2;
	
        /* supported versions */
	public static enum NEC765_VERSION {
		NEC765A,
		NEC765B,
		SMC37C78
	};
	
	/* read of data register */
	/*TODO*/////READ_HANDLER(nec765_data_r);
	/* write to data register */
	/*TODO*/////WRITE_HANDLER(nec765_data_w);
	/* read of main status register */
	/*TODO*/////READ_HANDLER(nec765_status_r);
		
	/* dma acknowledge with write */
	/*TODO*/////WRITE_HANDLER(nec765_dack_w);
	/* dma acknowledge with read */
	/*TODO*/////READ_HANDLER(nec765_dack_r);
	
	/* reset nec765 */
	void nec765_reset(int drive);
	
	/* reset pin of nec765 */
	void nec765_set_reset_state(int drive);
	
	
	/*********************/
	/* STATUS REGISTER 1 */
	
	/* this is set if a TC signal was not received after the sector data was read */
	public static final int NEC765_ST1_END_OF_CYLINDER = (1<<7);
	/* this is set if the sector ID being searched for is not found */
	public static final int NEC765_ST1_NO_DATA = (1<<2);
	/* set if disc is write protected and a write/format operation was performed */
	public static final int NEC765_ST1_NOT_WRITEABLE = (1<<1);
	
	/*********************/
	/* STATUS REGISTER 2 */
	
	/* C parameter specified did not match C value read from disc */
	public static final int NEC765_ST2_WRONG_CYLINDER = (1<<4);
	/* C parameter specified did not match C value read from disc, and C read from disc was 0x0ff */
	public static final int NEC765_ST2_BAD_CYLINDER = (1<<1);
	/* this is set if the FDC encounters a Deleted Data Mark when executing a read data
	command, or FDC encounters a Data Mark when executing a read deleted data command */
	public static final int NEC765_ST2_CONTROL_MARK = (1<<6);
}
