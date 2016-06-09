package address.events;

import address.model.datatypes.AddressBook;

/** Indicates some new data is available from the mirror*/
public class NewMirrorDataEvent extends BaseEvent {

    public AddressBook data;

    public NewMirrorDataEvent(AddressBook data){
        this.data = data;
    }

    @Override
    public String toString(){
        return "number of persons " + data.getPersons().size()  + ", number of tags " + data.getTags().size();
    }
}
