package com.github.lwhite1.tablesaw.columns;

import com.github.lwhite1.tablesaw.Table;
import com.github.lwhite1.tablesaw.api.ColumnType;
import com.github.lwhite1.tablesaw.filter.LocalDatePredicate;
import com.github.lwhite1.tablesaw.io.TypeUtils;
import com.github.lwhite1.tablesaw.mapper.DateMapUtils;
import com.github.lwhite1.tablesaw.store.ColumnMetadata;
import com.github.lwhite1.tablesaw.columns.packeddata.PackedLocalDate;
import com.github.lwhite1.tablesaw.columns.packeddata.PackedLocalDateTime;
import com.github.lwhite1.tablesaw.columns.packeddata.PackedLocalTime;
import com.github.lwhite1.tablesaw.filter.IntBiPredicate;
import com.github.lwhite1.tablesaw.filter.IntPredicate;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.roaringbitmap.RoaringBitmap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * A column in a base table that contains float values
 */
public class LocalDateColumn extends AbstractColumn implements DateMapUtils {

  public static final int MISSING_VALUE = (int) ColumnType.LOCAL_DATE.getMissingValue() ;

  private static final int DEFAULT_ARRAY_SIZE = 128;

  private IntArrayList data;

  private LocalDateColumn(String name) {
    super(name);
    data = new IntArrayList(DEFAULT_ARRAY_SIZE);
  }

  public LocalDateColumn(ColumnMetadata metadata) {
    super(metadata);
    data = new IntArrayList(DEFAULT_ARRAY_SIZE);
  }

  private LocalDateColumn(String name, int initialSize) {
    super(name);
    data = new IntArrayList(initialSize);
  }

  public int size() {
    return data.size();
  }

  @Override
  public ColumnType type() {
    return ColumnType.LOCAL_DATE;
  }

  public void add(int f) {
    data.add(f);
  }

  public IntArrayList data() {
    return data;
  }

  public void set(int index, int value) {
    data.set(index, value);
  }

  public void add(LocalDate f) {
    add(PackedLocalDate.pack(f));
  }

  @Override
  public String getString(int row) {
    return PackedLocalDate.toDateString(getInt(row));
  }

  @Override
  public LocalDateColumn emptyCopy() {
    return new LocalDateColumn(name());
  }

  @Override
  public void clear() {
    data.clear();
  }

  private LocalDateColumn copy() {
    return LocalDateColumn.create(name(), data);
  }

  @Override
  public void sortAscending() {
    Arrays.parallelSort(data.elements());
  }

  @Override
  public void sortDescending() {
    IntArrays.parallelQuickSort(data.elements(), reverseIntComparator);
  }

  IntComparator reverseIntComparator =  new IntComparator() {

    @Override
    public int compare(Integer o2, Integer o1) {
      return (o1 < o2 ? -1 : (o1.equals(o2) ? 0 : 1));
    }

    @Override
    public int compare(int o2, int o1) {
      return (o1 < o2 ? -1 : (o1 == o2 ? 0 : 1));
    }
  };

  @Override
  public int countUnique() {
    IntSet ints = new IntOpenHashSet(size());
    for (int i = 0; i < size(); i++) {
      ints.add(data.getInt(i));
    }
    return ints.size();
  }

  @Override
  public LocalDateColumn unique() {
    IntSet ints = new IntOpenHashSet(data.size());
    for (int i = 0; i < size(); i++) {
      ints.add(data.getInt(i));
    }
    return LocalDateColumn.create(name() + " Unique values", IntArrayList.wrap(ints.toIntArray()));
  }

  public LocalDate firstElement() {
    if (isEmpty()) {
      return null;
    }
    return PackedLocalDate.asLocalDate(getInt(0));
  }

  public LocalDate max() {
    int max;
    int missing = Integer.MIN_VALUE;
    if (!isEmpty()) {
      max = getInt(0);
    } else {
      return null;
    }
    for (int aData : data) {
      if (missing != aData) {
        max = (max > aData) ? max : aData;
      }
    }

    if (missing == max) {
      return null;
    }
    return PackedLocalDate.asLocalDate(max);
  }

  public LocalDate min() {
    int min;
    int missing = Integer.MIN_VALUE;

    if (!isEmpty()) {
      min = getInt(0);
    } else {
      return null;
    }
    for (int aData : data) {
      if (missing != aData) {
        min = (min < aData) ? min : aData;
      }
    }
    if (Integer.MIN_VALUE == min) {
      return null;
    }
    return PackedLocalDate.asLocalDate(min);
  }

  public CategoryColumn dayOfWeek() {
    CategoryColumn newColumn = CategoryColumn.create(this.name() + " day of week");
    for (int r = 0; r < this.size(); r++) {
      int c1 = this.getInt(r);
      if (c1 == LocalDateColumn.MISSING_VALUE) {
        newColumn.add(null);
      } else {
        newColumn.add(PackedLocalDate.getDayOfWeek(c1).toString());
      }
    }
    return newColumn;
  }

  public IntColumn dayOfMonth() {
    IntColumn newColumn = IntColumn.create(this.name() + " day of month");
    for (int r = 0; r < this.size(); r++) {
      int c1 = this.getInt(r);
      if (c1 == LocalDateColumn.MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDate.getDayOfMonth(c1));
      }
    }
    return newColumn;
  }

  public IntColumn dayOfYear() {
    IntColumn newColumn = IntColumn.create(this.name() + " day of month");
    for (int r = 0; r < this.size(); r++) {
      int c1 = this.getInt(r);
      if (c1 == LocalDateColumn.MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDate.getDayOfYear(c1));
      }
    }
    return newColumn;
  }

  public IntColumn month() {
    IntColumn newColumn = IntColumn.create(this.name() + " month");

    for (int r = 0; r < this.size(); r++) {
      int c1 = this.getInt(r);
      if (c1 == LocalDateColumn.MISSING_VALUE) {
        newColumn.add(IntColumn.MISSING_VALUE);
      } else {
        newColumn.add(PackedLocalDate.getMonthValue(c1));
      }
    }
    return newColumn;
  }

  public LocalDate get(int index) {
    return PackedLocalDate.asLocalDate(getInt(index));
  }

  public static LocalDateColumn create(String name) {
    return new LocalDateColumn(name);
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public IntComparator rowComparator() {
    return comparator;
  }

    IntComparator comparator = new IntComparator() {

    @Override
    public int compare(Integer r1, Integer r2) {
      return compare((int) r1, (int) r2);
    }

    @Override
    public int compare(int r1, int r2) {
      int f1 = getInt(r1);
      int f2 = getInt(r2);
      return Integer.compare(f1, f2);
    }
  };

  public static LocalDateColumn create(String columnName, IntArrayList dates) {
    LocalDateColumn column = new LocalDateColumn(columnName, dates.size());
    column.data = dates;
    return column;
  }

  public static int convert(String value) {
    if (Strings.isNullOrEmpty(value)
        || TypeUtils.MISSING_INDICATORS.contains(value)
        || value.equals("-1")) {
      return (int) ColumnType.LOCAL_DATE.getMissingValue();
    }
    value = Strings.padStart(value, 4, '0');
    return PackedLocalDate.pack(LocalDate.parse(value, TypeUtils.DATE_FORMATTER));
  }

  public void addCell(String string) {
    try {
      add(convert(string));
    } catch (NullPointerException e) {
      throw new RuntimeException(name() + ": "
          + string + ": "
          + e.getMessage());
    }
  }

  public int getInt(int index) {
    return data.getInt(index);
  }

  public RoaringBitmap isEqualTo(LocalDate value) {
      int packed = PackedLocalDate.pack(value);
      return apply(IntColumnUtils.isEqualTo, packed);
  }

  public RoaringBitmap isEqualTo(LocalDateColumn column) {
    RoaringBitmap results = new RoaringBitmap();
    int i = 0;
    IntIterator intIterator = column.iterator();
    for (int next : data) {
      if (next == intIterator.nextInt()) {
        results.add(i);
      }
      i++;
    }
    return results;
  }

  /**
   * Returns a table of dates and the number of observations of those dates
   */
  @Override
  public Table summary() {

    // TODO(lwhite): This is not a very useful summary. Fix it

    Int2IntOpenHashMap counts = new Int2IntOpenHashMap();

    for (int i = 0; i < size(); i++) {
      int value;
      int next = getInt(i);
      if (next == Integer.MIN_VALUE) {
        value = LocalDateColumn.MISSING_VALUE;
      } else {
        value = next;
      }
      if (counts.containsKey(value)) {
        counts.addTo(value, 1);
      } else {
        counts.put(value, 1);
      }
    }
    Table table = new Table("Column: " + name());
    table.addColumn(LocalDateColumn.create("Date"));
    table.addColumn(IntColumn.create("Count"));

    for (Int2IntMap.Entry entry : counts.int2IntEntrySet()) {
      table.localDateColumn(0).add(entry.getIntKey());
      table.intColumn(1).add(entry.getIntValue());
    }
    table = table.sortDescendingOn("Count");

    return table.head(5);
  }

  public LocalDateTimeColumn atTime(LocalTimeColumn c) {
    LocalDateTimeColumn newColumn = LocalDateTimeColumn.create(this.name() + " " + c.name());
    for (int r = 0; r < this.size(); r++) {
      int c1 = this.getInt(r);
      int c2 = c.getInt(r);
      if (c1 == MISSING_VALUE || c2 == LocalTimeColumn.MISSING_VALUE) {
        newColumn.add(LocalDateTimeColumn.MISSING_VALUE);
      } else {
        LocalDate value1 = PackedLocalDate.asLocalDate(c1);
        LocalTime time = PackedLocalTime.asLocalTime(c2);
        newColumn.add(PackedLocalDateTime.pack(value1, time));
      }
    }
    return newColumn;
  }

  public RoaringBitmap isAfter(int value) {
    return apply(PackedLocalDate::isAfter, value);
  }

  public RoaringBitmap isAfter(LocalDate value) {
    int packed = PackedLocalDate.pack(value);
    return apply(PackedLocalDate::isAfter, packed);
  }

  public RoaringBitmap isBefore(int value) {
    return apply(PackedLocalDate::isBefore, value);
  }

  public RoaringBitmap isBefore(LocalDate value) {
    int packed = PackedLocalDate.pack(value);
    return apply(PackedLocalDate::isBefore, packed);
  }

  public RoaringBitmap isMonday() {
    return apply(PackedLocalDate::isMonday);
  }

  public RoaringBitmap isTuesday() {
    return apply(PackedLocalDate::isTuesday);
  }

  public RoaringBitmap isWednesday() {
    return apply(PackedLocalDate::isWednesday);
  }

  public RoaringBitmap isThursday() {
    return apply(PackedLocalDate::isThursday);
  }

  public RoaringBitmap isFriday() {
    return apply(PackedLocalDate::isFriday);
  }

  public RoaringBitmap isSaturday() {
    return apply(PackedLocalDate::isSaturday);
  }

  public RoaringBitmap isSunday() {
    return apply(PackedLocalDate::isSunday);
  }

  public RoaringBitmap isInJanuary() {
    return apply(PackedLocalDate::isInJanuary);
  }

  public RoaringBitmap isInFebruary() {
    return apply(PackedLocalDate::isInFebruary);
  }

  public RoaringBitmap isInMarch() {
    return apply(PackedLocalDate::isInMarch);
  }

  public RoaringBitmap isInApril() {
    return apply(PackedLocalDate::isInApril);
  }

  public RoaringBitmap isInMay() {
    return apply(PackedLocalDate::isInMay);
  }

  public RoaringBitmap isInJune() {
    return apply(PackedLocalDate::isInJune);
  }

  public RoaringBitmap isInJuly() {
    return apply(PackedLocalDate::isInJuly);
  }

  public RoaringBitmap isInAugust() {
    return apply(PackedLocalDate::isInAugust);
  }

  public RoaringBitmap isInSeptember() {
    return apply(PackedLocalDate::isInSeptember);
  }

  public RoaringBitmap isInOctober() {
    return apply(PackedLocalDate::isInOctober);
  }

  public RoaringBitmap isInNovember() {
    return apply(PackedLocalDate::isInNovember);
  }

  public RoaringBitmap isInDecember() {
    return apply(PackedLocalDate::isInDecember);
  }

  public RoaringBitmap isFirstDayOfMonth() {
    return apply(PackedLocalDate::isFirstDayOfMonth);
  }

  public RoaringBitmap isLastDayOfMonth() {
    return apply(PackedLocalDate::isLastDayOfMonth);
  }

  public RoaringBitmap isInQ1() {
    return apply(PackedLocalDate::isInQ1);
  }

  public RoaringBitmap isInQ2() {
    return apply(PackedLocalDate::isInQ2);
  }

  public RoaringBitmap isInQ3() {
    return apply(PackedLocalDate::isInQ3);
  }

  public RoaringBitmap isInQ4() {
    return apply(PackedLocalDate::isInQ4);
  }

  public RoaringBitmap isInYear(int year) {
    return apply(PackedLocalDate::isInYear, year);
  }

  public String print() {
    StringBuilder builder = new StringBuilder();
    builder.append(title());
    for (int next : data) {
      builder.append(String.valueOf(PackedLocalDate.asLocalDate(next)));
      builder.append('\n');
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return "LocalDate column: " + name();
  }

  @Override
  public void append(Column column) {
    Preconditions.checkArgument(column.type() == this.type());
    LocalDateColumn intColumn = (LocalDateColumn) column;
    for (int i = 0; i < intColumn.size(); i++) {
      add(intColumn.getInt(i));
    }
  }

  public LocalDateColumn selectIf(LocalDatePredicate predicate) {
    LocalDateColumn column = emptyCopy();
    IntIterator iterator = iterator();
    while(iterator.hasNext()) {
      int next = iterator.nextInt();
      if (predicate.test(PackedLocalDate.asLocalDate(next))) {
        column.add(next);
      }
    }
    return column;
  }

  /**
   * This version operates on predicates that treat the given IntPredicate as operating on a packed local time
   * This is much more efficient that using a LocalTimePredicate, but requires that the developer understand the
   * semantics of packedLocalTimes
   */
  public LocalDateColumn selectIf(IntPredicate predicate) {
    LocalDateColumn column = emptyCopy();
    IntIterator iterator = iterator();
    while(iterator.hasNext()) {
      int next = iterator.nextInt();
      if (predicate.test(next)) {
        column.add(next);
      }
    }
    return column;
  }

  //TODO(lwhite): Implement
  @Override
  public LocalDateColumn max(int n) {
    return null;
  }

  //TODO(lwhite): Implement
  @Override
  public LocalDateColumn min(int n) {
    return null;
  }

  public IntIterator iterator() {
    return data.iterator();
  }

  public RoaringBitmap apply(IntPredicate predicate) {
    RoaringBitmap bitmap = new RoaringBitmap();
    for(int idx = 0; idx < data.size(); idx++) {
      int next = data.getInt(idx);
      if (predicate.test(next)) {
        bitmap.add(idx);
      }
    }
    return bitmap;
  }

  public RoaringBitmap apply(IntBiPredicate predicate, int value) {
    RoaringBitmap bitmap = new RoaringBitmap();
    for(int idx = 0; idx < data.size(); idx++) {
      int next = data.getInt(idx);
      if (predicate.test(next, value)) {
        bitmap.add(idx);
      }
    }
    return bitmap;
  }
}