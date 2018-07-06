package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedList;

public class History {

    private LinkedList<HistoryElement> timeline;
    private int size;
    private int curser;

    public History(int size)
        {this.timeline = new LinkedList<>();
         this.size = size;
         curser = 0;
        }

    /**
     * Inserts a new HistoryElement into the timeline
     * @param file path to the changed ABS-File
     * @param doc current document in the editorPane
     */
    public void insert(@Nonnull File file, @Nullable StringBuffer doc)
        {HistoryElement newElem = new HistoryElement(file, doc);
         if(timeline.size() >= size)
            {timeline.pollLast();}
         if(curser != 0)
            {for(int i = 0; i < curser; i++)
                {timeline.pollFirst();}
             curser = 0;
            }
         timeline.push(newElem);
        }


    /**
     * Returns the latest element or the next undo-Element
     * @return the latest HistoryElement
     */
    @Nullable
    public HistoryElement undo()
        {HistoryElement response = timeline.get(curser);
         curser++;
         return response;
        }

    /**
     * returns the next redo-Element if existing, otherwise null
      * @return the next redo-Element
     */
    @Nullable
    public HistoryElement redo()
        {if(curser == 0) {return null;}
         curser--;
         HistoryElement response = timeline.get(curser);

         return response;
        }

    /**
     * checks if a undo is possible
      * @return true, if a undo is possible, otherwise false
     */
    public boolean canUndo()
        {return timeline.size() > curser && timeline.size() != 0;}

    /**
     * checks if a redo is possible
     * @return true, if a redo is possible, otherwise false
     */
    public boolean canRedo()
        {return curser > 0 && timeline.size() != 0;}
}
