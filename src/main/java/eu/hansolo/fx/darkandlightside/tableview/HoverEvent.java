/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.darkandlightside.tableview;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;


/**
 * Created by hansolo on 07.07.16.
 */
public class HoverEvent extends Event {
    public static final EventType<HoverEvent> CURRENT_ROW = new EventType(ANY, "currentRow");
    public static final EventType<HoverEvent> CURRENT_COL = new EventType(ANY, "currentCol");

    final public TableRow    TABLE_ROW;
    final public TableColumn TABLE_COL;


    public HoverEvent(final EventType<HoverEvent> TYPE, final TableRow ROW) {
        super(TYPE);
        TABLE_ROW = ROW;
        TABLE_COL = null;
    }
    public HoverEvent(final EventType<HoverEvent> TYPE, final TableColumn COL) {
        super(TYPE);
        TABLE_ROW = null;
        TABLE_COL = COL;
    }
}
