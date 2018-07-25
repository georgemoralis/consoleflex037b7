package mess.includes;

import static mess.includes.flopdrvH.*;

public interface dskH {
    public abstract int dsk_floppy_load(int id);
    public int dsk_floppy_id(int id);
    public void dsk_floppy_exit(int id);
    public void dsk_seek_callback(int drive, int physical_track);
    public void dsk_get_id_callback(int drive, chrn_id id, int id_index, int side);
    public int dsk_get_sectors_per_track(int drive, int side);

    public void dsk_write_sector_data_from_buffer(int drive, int sector_index, int side, char[] ptr, int length);
    public void dsk_read_sector_data_into_buffer(int drive, int sector_index, int side, char[] ptr, int length);
}
