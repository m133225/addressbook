package address.events;

import java.io.File;

/**
 * Indicates an exception during a file saving
 */
public class FileSavingExceptionEvent extends BaseEvent {

    public Exception exception;
    public File file;

    public FileSavingExceptionEvent(Exception exception, File file){
        this.exception = exception;
        this.file = file;
    }

    @Override
    public String toString(){
        return "" + exception;
    }
}
