package cz.cvut.fel.horovtom.jasl.interpreter;

import java.util.ArrayList;

class JASLList extends ArrayList<Object> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int size = this.size();
        for (int i = 0; i < size - 1; i++) {
            Object o = this.get(i);
            sb.append(o).append(", ");
        }
        if (size > 0)
            sb.append(this.get(size() - 1));
        sb.append("}");
        return sb.toString();
    }
}
