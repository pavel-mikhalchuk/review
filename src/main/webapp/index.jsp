<%@page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<title>Review</title>
<style>
    .active {
        background-color: yellow;
    }

    .row {
        height: 15px;
        /*border-top: solid 1px white;*/
        border-bottom: solid 1px white;
    }

    .number {
        background-color: #f3f3f3;
        border-bottom: solid 1px #f3f3f3;
    }

    .modified {
        background-color: #bfd5ff;
        border-bottom: solid 1px #bfd5ff;
    }

    .added {
        background-color: #98fcb7;
        border-bottom: solid 1px #98fcb7;
    }

    .deleted {
        background-color: #a9a9a9;
        border-bottom: solid 1px #a9a9a9;
    }

    .first {
        border-top: solid 1px brown;
    }

    .last {
        border-bottom: solid 1px #acacac;
    }

    #right-scroller {
        width: 8px;
        background-color: #c1c1c1;
        float: right;
        opacity: 0.4;
        cursor: pointer;
    }

    #right-scroller:hover {
        opacity: 0.7;
    }

    #left-scroller {
        width: 8px;
        background-color: #c1c1c1;
        float: left;
        opacity: 0.4;
        cursor: pointer;
    }

    #left-scroller:hover {
        opacity: 0.7;
    }
</style>
<script type="text/javascript" src="js/jquery-1.9.js"></script>
<script type="text/javascript" src="js/jquery.mousewheel.js"></script>
<script type="text/javascript" src="js/diff_match_patch_uncompressed.js"></script>
<script type="text/javascript">
var revMap = {};
var ctrPressed = false;

function loadLog() {
    $('#log').html('Loading...');
    $.get('review/log', function (data) {
        var table = '<table border="1"><tr><th>Revision</th><th>Author</th><th>Date</th><th>Message</th></tr>';
        for (var i = 0; i < data.logentry.length; i++) {
            var entry = data.logentry[i];
            table += '<tr><td>' + entry.revision + '</td><td>' + entry.author + '</td><td>' + entry.date + '</td>' +
                    '<td>' + entry.msg + '</td><td><input type="button" value="->" onclick="showRevision(' + entry.revision + ');"/></td></tr>';
            revMap[entry.revision] = entry;
        }
        $('#log').html(table + '</table>');
    });
}

function showRevision(revision) {
    $('#revision').html('<div class="row" action="' + revMap[revision].path[0].action + '" rev="' + revision + '" file="' + revMap[revision].path[0].value + '"  style="padding: 5px 0;">' + revMap[revision].path[0].value + ':' + revMap[revision].path[0].action + '</div>');
    for (var j = 1; j < revMap[revision].path.length; j++) {
        var path = revMap[revision].path[j];
        $('#revision').append('<div class="row" action="' + path.action + '" rev="' + revision + '" file="' + path.value + '" style="padding: 5px 0; border-top: dotted 1px;">' + path.value + ':' + path.action + '</div>');
    }
    $('.row').hover(function () {
        $(this).toggleClass('active');
    });
}

function showDiff(action, file, rev) {
    if ($('.active').length > 0) {
        if (action == 'A') {
            $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                $('#left').html(data);
            });
        } else if (action == 'D') {
            $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                $('#right').html(data);
            });
        } else {
            $.get('review/diff?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                diff(data);
            })
        }
    }
}

var _diff;

var leftByLine = {}; //
var leftByRealLine = {}; //
var leftLineCount;
var leftFirst = 1;
var leftLast = 40;
var leftLineByRealLine = {};
var leftRealLineByLine = {};

var rightByLine = {}; //
var rightByRealLine = {}; //
var rightLineCount;
var rightFirst = 1;
var rightLast = 40;
var rightLineByRealLine = {};
var rightRealLineByLine = {};

var maxRow = 17;
var downOver = 6;

var leftLocked = false;
var rightLocked = false;
var prevY = 0;

var WORDS_REG_EXP = initWordsCuttingPointsRegExp();

function initWordsCuttingPointsRegExp() {
    var points = [' ', '\\(', '\\)', '\\.', ':', ';', ',', '\\{', '\\}', '\\<', '\\>', '\\/', '\\|', '\\\\', '\\+', '\\-', '\\*'];
    return new RegExp('(' + points.join("|") + ')', 'g')
}

function diff(diff) {
    _diff = prettyDiff(diff);

    var left = writeSide(diff.left, 'left');
    $('#left-content').html('<pre style="font-family: inherit; display: inline-block; margin: 0;">' + left.lines + '</pre>');
    $('#left-middle').html('<pre style="margin: 0; color: #a52a2a;">' + left.middle + '</pre>');
    $('#left-line').html('<pre style="margin: 0; color: #a52a2a;">' + left.numbers + '</pre>');
    leftLineCount = left.lineCount;
    leftLineByRealLine = left.lineByRealLine;
    leftRealLineByLine = left.realLineByLine;
    setLeftRow(1 - leftFirst);
    initLeftScroller();
    $('#left').unmousewheel(scrollLeft);
    $('#left').mousewheel(scrollLeft);

    var right = writeSide(diff.right, 'right');
    $('#right-content').html('<pre style="font-family: inherit; display: inline-block; margin: 0;">' + right.lines + '</pre>');
    $('#right-middle').html('<pre style="margin: 0; color: #a52a2a;">' + right.middle + '</pre>');
    $('#right-line').html('<pre style="margin: 0; color: #a52a2a;">' + right.numbers + '</pre>');
    rightLineCount = right.lineCount;
    rightLineByRealLine = right.lineByRealLine;
    rightRealLineByLine = right.realLineByLine;
    setRightRow(1 - rightFirst);
    initRightScroller();
    $('#right').unmousewheel(scrollRight);
    $('#right').mousewheel(scrollRight);

    setScrolledLeft(1);
    setScrolledRight(1);

    writeConnections();

    $(document).mouseup(function () {
        leftLocked = false;
        rightLocked = false;
    });

    $(document).mousemove(function (e) {
        if (leftLocked) {
            moveLeftScroller(e);
        } else if (rightLocked) {
            moveRightScroller(e);
        }
    });
}

function prettyDiff(diff) {
    var l = diff.left;
    var r = diff.right;
    var d;

    for (var i = 0; i < l.length; i++) {
        if (l[i].action == '!') {
            d = prettySides(l[i].line, r[i].line);
            l[i].pretty = d.left;
            r[i].pretty = d.right;
        }
    }

    return diff;
}

function prettySides(s1, s2) {
    return prettySidesUnicode(toUnicode(s1, s2));
}

function toUnicode(s1, s2) {
    var w1 = toWords(s1);
    var w2 = toWords(s2);

    var start = 0x2a00;

    var x = '';
    var y = '';
    var cache = {};
    var mirror = {};

    for (var i = 0; i < w1.length; i++) {
        if (cache[w1[i]] == undefined) {
            cache[w1[i]] = String.fromCharCode(start++);
            mirror[cache[w1[i]]] = w1[i];
        }
        x += cache[w1[i]];
    }

    for (var j = 0; j < w2.length; j++) {
        if (cache[w2[j]] == undefined) {
            cache[w2[j]] = String.fromCharCode(start++);
            mirror[cache[w2[j]]] = w2[j];
        }
        y += cache[w2[j]];
    }

    return { x: x, y: y, mirror: mirror }
}

function toWords(s) {
    var array = s.split(WORDS_REG_EXP);
    return array.length > 0 && array[0] == '' ? array.slice(1) : array;
}

function prettySidesUnicode(unicode) {
    var dmp = new diff_match_patch();

    var d = dmp.diff_main(unicode.x, unicode.y, false);

    var td = [];
    var d1 = [];
    var d2 = [];

    for (var i = 0; i < d.length; i++) {
        if (d[i][0] == 0) {
            if (td.length == 2) {
                d1.push(['!', td[0][1]]);
                d2.push(['!', td[1][1]]);
                td = [];
            } else if (td.length == 1) {
                if (td[0][0] == -1) {
                    d1.push(td[0]);
                    d2.push([-11, '']);
                } else if (td[0][0] == 1) {
                    d1.push([11, '']);
                    d2.push(td[0]);
                }
                td = [];
            }
            d1.push(d[i]);
            d2.push(d[i]);
        } else {
            td.push(d[i]);
        }
    }

    if (td.length == 2) {
        d1.push(['!', td[0][1]]);
        d2.push(['!', td[1][1]]);
        td = [];
    } else if (td.length == 1) {
        if (td[0][0] == -1) {
            d1.push(td[0]);
            d2.push([-11, '']);
        } else if (td[0][0] == 1) {
            d1.push([11, '']);
            d2.push(td[0]);
        }
        td = [];
    }

    return {left: prettySideUnicode(d1, unicode.mirror), right: prettySideUnicode(d2, unicode.mirror) };
}

function prettySideUnicode(d, mirror) {
    var s = "";
    for (var i = 0; i < d.length; i++) {
        if (d[i][0] == 0) {
            s += safeTags(fromUnicode(d[i][1], mirror));
        } else if (d[i][0] == -1) {
            s += '<span style="background-color: #bdbdbd;">' + safeTags(fromUnicode(d[i][1], mirror)) + '</span>';
        } else if (d[i][0] == -11) {
            s += '<span style="border: solid 1px #bdbdbd;"></span>';
        } else if (d[i][0] == 1) {
            s += '<span style="background-color: #98fcb7;">' + safeTags(fromUnicode(d[i][1], mirror)) + '</span>';
        } else if (d[i][0] == 11) {
            s += '<span style="border: solid 1px #98fcb7;"></span>';
        } else if (d[i][0] == '!') {
            s += '<span style="background-color: #b3c4ed;">' + safeTags(fromUnicode(d[i][1], mirror)) + '</span>';
        }
    }
    return s;
}

function fromUnicode(s, mirror) {
    var res = '';
    for (var i = 0; i < s.length; i++) {
        res += mirror[s.charAt(i)];
    }
    return res;
}

function moveLeftScroller(e) {
    if (Math.abs(e.pageY - prevY) * (leftLineCount / 640) > 1) {
        var d = (e.pageY - prevY) * (leftLineCount / 640);
        var rowsDelta = e.pageY - prevY > 0 ? Math.floor(d) : Math.ceil(d);
        $('#left-out').html('ssss~~~ ' + d + ' - ' + rowsDelta);
        if (rowsDelta == 0 || leftFirst + rowsDelta <= 0 || leftLast + rowsDelta > leftLineCount + downOver) rowsDelta = 0;
        if (rowsDelta != 0) {
            scrollLeftByDelta(rowsDelta);
            prevY = e.pageY;
        }
    }
}

function moveRightScroller(e) {
    if (Math.abs(e.pageY - prevY) * (rightLineCount / 640) > 1) {
        var d = (e.pageY - prevY) * (rightLineCount / 640);
        var rowsDelta = e.pageY - prevY > 0 ? Math.floor(d) : Math.ceil(d);
        $('#right-out').html('ssss~~~ ' + d + ' - ' + rowsDelta);
        if (rowsDelta == 0 || rightFirst + rowsDelta <= 0 || rightLast + rowsDelta > rightLineCount + downOver) rowsDelta = 0;
        if (rowsDelta != 0) {
            scrollRightByDelta(rowsDelta);
            prevY = e.pageY;
        }
    }
}

function writeSide(lines, side) {
    var border = false;

    var lineByRealLine = {};
    var realLineByLine = {};
    var lineCount = 1;

    var line = lines[0];
    lineByRealLine['1'] = {'number': 1, 'scrolled': false};
    realLineByLine['1'] = {'number': 1, 'scrolled': false};

    var wl = writeLine(line, lineCount, (lines[1].action == '+' || lines[1].action == '-' || lines[1].action == '!'), 1, side);
    var linesHtml = wl.html;
    var middleHtml = wl.middle;
    var numbersHtml = wl.number;
    lineCount++;

    for (var i = 1; i < lines.length - 1; i++) {
        line = lines[i];
        lineByRealLine[lineCount] = {'number': i + 1, 'scrolled': false};
        realLineByLine[i + 1] = {'number': lineCount, 'scrolled': false};

        if (!emptyLine(lines[i])) {
            if (i != lines.length - 1) {
                if (lines[i + 1].action == '+' || lines[i + 1].action == '-' || lines[i + 1].action == '!') {
                    if (lines[i + 1].action != line.action) border = true;
                }
            }

            if (line.action == '+' || line.action == '-' || line.action == '!') {
                if (i == lines.length - 1 || lines[i + 1].action != line.action) border = true;
            }

            wl = writeLine(line, lineCount, border, i + 1, side);
            linesHtml += wl.html;
            middleHtml += wl.middle;
            numbersHtml += wl.number;
            lineCount++;
            border = false;
        }
    }

    for (i = 0; i < 7; i++) {
        middleHtml += '<div class="row number"></div>';
        numbersHtml += '<div class="row number"></div>';
    }

    return { lines: linesHtml, middle: middleHtml, numbers: numbersHtml, lineCount: lineCount - 1, lineByRealLine: lineByRealLine, realLineByLine: realLineByLine };
}

function emptyLine(line) {
    return (line.action == '+' && line.line == '') || (line.action == '-' && line.line == '');
}

function initLeftScroller() {
    $('#left-scroll').html('<div id="left-scroller"></div>');
    $('#left-scroller').height(Math.floor(640 * 640 / (16 * (leftLineCount + downOver))));
    $('#left-scroller').mousedown(function (e) {
        leftLocked = true;
        prevY = e.pageY;
        e.preventDefault();
    });

    var changes = '';
    var rowHeight = 640 / (leftLineCount + downOver);
    for (var i = 0; i < _diff.left.length; i++) {
        var l = _diff.left[i];
        if (l.action == '!') {
            changes += '<div onclick="moveToLeftRow(' + leftRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; margin-left: 13px; background-color: #bfd5ff; position: absolute; width: 4px; height: ' + (rowHeight - 1) + 'px; top: ' + rowHeight * (leftRealLineByLine[l.number].number) + 'px;"></div>';
        } else if (l.action == '+' && l.line == '') {
            changes += '<div onclick="moveToLeftRow(' + leftRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; margin-left: 13px; background-color: #98fcb7; position: absolute; width: 4px; height: ' + 3 + 'px; top: ' + rowHeight * (leftRealLineByLine[l.number].number) + 'px;"></div>';
        } else if (l.action == '-') {
            changes += '<div onclick="moveToLeftRow(' + leftRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; margin-left: 13px; background-color: #a9a9a9; position: absolute; width: 4px; height: ' + (rowHeight - 1) + 'px; top: ' + rowHeight * (leftRealLineByLine[l.number].number) + 'px;"></div>';
        }
    }
    $('#left-scroll').append(changes);
}

function moveToLeftRow(row) {
    var delta = row - (leftFirst + maxRow);
    if (delta > 0) {
        for (var i = 0; i < delta; i++) {
            scrollLeftByDelta(1);
        }
    } else if (delta < 0) {
        for (var i = 0; i > delta; i--) {
            scrollLeftByDelta(-1);
        }
    }
}

function initRightScroller() {
    $('#right-scroll').html('<div id="right-scroller" style="position: relative;"></div>');
    $('#right-scroller').height(Math.floor(640 * 640 / (16 * (rightLineCount + downOver))));
    $('#right-scroller').mousedown(function (e) {
        rightLocked = true;
        prevY = e.pageY;
        e.preventDefault();
    });

    var changes = '';
    var rowHeight = 640 / (rightLineCount + downOver);
    for (var i = 0; i < _diff.right.length; i++) {
        var l = _diff.right[i];
        if (l.action == '!') {
            changes += '<div onclick="moveToRightRow(' + rightRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; background-color: #bfd5ff; position: absolute; width: 4px; height: ' + (rowHeight - 1) + 'px; top: ' + rowHeight * (rightRealLineByLine[l.number].number) + 'px;"></div>';
        } else if (l.action == '+') {
            changes += '<div onclick="moveToRightRow(' + rightRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; background-color: #98fcb7; position: absolute; width: 4px; height: ' + (rowHeight - 1) + 'px; top: ' + rowHeight * (rightRealLineByLine[l.number].number) + 'px;"></div>';
        } else if (l.action == '-') {
            changes += '<div onclick="moveToRightRow(' + rightRealLineByLine[l.number].number + ')" style="border-top: solid 1px white; border-left: solid 1px white; border-right: solid 1px #acacac; border-bottom: solid 1px #acacac; cursor: pointer; background-color: #a9a9a9; position: absolute; width: 4x; height: ' + 3 + 'px; top: ' + rowHeight * (rightRealLineByLine[l.number].number) + 'px;"></div>';
        }
    }
    $('#right-scroll').append(changes);
}

function moveToRightRow(row) {
    var delta = row - (rightFirst + maxRow);
    if (delta > 0) {
        for (var i = 0; i < delta; i++) {
            scrollRightByDelta(1);
        }
    } else if (delta < 0) {
        for (var i = 0; i > delta; i--) {
            scrollRightByDelta(-1);
        }
    }
}

function writeLine(line, number, border, i, id) {
    if (line.action == '+') {
        return { html: '<div id="line-' + id + '-' + number + '" class="row added' + (border == true ? ' last">' : '">') + safeTags(line.line) + '</div>', middle: '<div id="middle-' + id + '-' + number + '" class="row added' + (border == true ? ' last">' : '">') + '</div>', number: '<div id="num-' + id + '-' + number + '" class="row added' + (border == true ? ' last">' : '">') + number + '</div>' };
    } else if (line.action == '-') {
        return { html: '<div id="line-' + id + '-' + number + '" class="row deleted' + (border == true ? ' last">' : '">') + safeTags(line.line) + '</div>', middle: '<div id="middle-' + id + '-' + number + '" class="row deleted' + (border == true ? ' last">' : '">') + '</div>', number: '<div id="num-' + id + '-' + number + '" class="row deleted' + (border == true ? ' last">' : '">') + number + '</div>' };
    } else if (line.action == '!') {
        return { html: '<div id="line-' + id + '-' + number + '" class="row modified' + (border == true ? ' last">' : '">') + line.pretty + '</div>', middle: '<div id="middle-' + id + '-' + number + '" class="row modified' + (border == true ? ' last">' : '">') + '</div>', number: '<div id="num-' + id + '-' + number + '" class="row modified' + (border == true ? ' last">' : '">') + number + '</div>' };
    }
    return { html: '<div id="line-' + id + '-' + number + '" class="row' + (border == true ? ' last">' : '">') + safeTags(line.line) + '</div>', middle: '<div id="middle-' + id + '-' + number + '" class="row number' + (border == true ? ' last">' : '">') + '</div>', number: '<div id="num-' + id + '-' + number + '" class="row number' + (border == true ? ' last">' : '">') + number + '</div>' };
}

function safeTags(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function scrollLeft(event, delta, deltaX, deltaY) {
    var rowsDelta = 0;

    if (deltaY < 0) {
        rowsDelta += 1;
    } else if (deltaY > 0) {
        rowsDelta += -1;
    }

    scrollLeftByDelta(rowsDelta);
}

function scrollLeftByDelta(rowsDelta) {
    if (rowsDelta == 0 || leftFirst + rowsDelta <= 0 || leftLast + rowsDelta > leftLineCount + downOver) rowsDelta = 0;

    if (rowsDelta != 0) {
        if (rightLast + rowsDelta <= rightLineCount + downOver && rightFirst + rowsDelta > 0) {
            var row;
            if (rowsDelta > 0) {
                if (rightLast != rightLineCount + downOver) {
                    for (row = leftFirst; row <= leftFirst + maxRow; row++) {
                        if (rightRealLineByLine[leftLineByRealLine[row].number].number == rightRealLineByLine[leftLineByRealLine[row].number + 1].number) {
                            if (leftLineByRealLine[row].scrolled == false) {
                                leftLineByRealLine[row].scrolled = true;
                                row = -1;
                                break;
                            }
                        }
                    }
                } else {
                    for (row = leftFirst + maxRow; row <= leftLast; row++) {
                        if (rightRealLineByLine[leftLineByRealLine[row].number].number == rightRealLineByLine[leftLineByRealLine[row].number + 1].number) {
                            if (leftLineByRealLine[row].scrolled == false) {
                                leftLineByRealLine[row].scrolled = true;
                                row = -1;
                                break;
                            }
                        }
                    }
                }
                if (row != -1) row--;
            } else {
                if (rightFirst != 1) {
                    for (row = leftLast; row >= leftFirst + maxRow - 1; row--) {
                        if (row <= leftLast - downOver) {
                            if (leftLineByRealLine[row].scrolled == true) {
                                leftLineByRealLine[row].scrolled = false;
                                row = -1;
                                break;
                            }
                        }
                    }
                } else {
                    for (row = leftLast + maxRow - 1; row >= leftFirst; row--) {
                        if (row <= leftLast - downOver) {
                            if (leftLineByRealLine[row].scrolled == true) {
                                leftLineByRealLine[row].scrolled = false;
                                row = -1;
                                break;
                            }
                        }
                    }
                }
                if (row != -1) row++;
            }
            if (row != -1) {
                setRightRow((leftLineByRealLine[row].number - rightLineByRealLine[rightFirst + (row - leftFirst)].number) + rowsDelta);
                setScrolledRight(row);
            }
        }
        setLeftRow(rowsDelta);

    }

    $('#left-out').html('~~~~~' + ':' + leftFirst + ':' + leftLast + ':' + leftLineCount);

    event.preventDefault();

    writeConnections();
}

function setLeftRow(rowsDelta) {
    if (rowsDelta == 0) return;

    var c = $('#left-content').offset();
    var m = $('#left-middle').offset();
    var l = $('#left-line').offset();
    var s = $('#left-scroller').offset();

    $('#left-content').offset({top: c.top - 16 * rowsDelta, left: c.left});
    $('#left-middle').offset({top: m.top - 16 * rowsDelta, left: m.left});
    $('#left-line').offset({top: l.top - 16 * rowsDelta, left: l.left});
    $('#left-scroller').offset({top: s.top + (640 / (leftLineCount + downOver) * rowsDelta), left: s.left});

    leftFirst += rowsDelta;
    leftLast += rowsDelta;
}

function setScrolledLeft(row) {
    var r = leftRealLineByLine[rightLineByRealLine[row].number].number;
    for (var i = 1; i < leftLineCount; i++) {
        if (rightRealLineByLine[leftLineByRealLine[i].number].number == rightRealLineByLine[leftLineByRealLine[i].number + 1].number) {
            leftLineByRealLine[i].scrolled = i < r;
//            $('#middle-left-' + i).html(i < r);
        }
    }
}

function scrollRight(event, delta, deltaX, deltaY) {
    var rowsDelta = 0;

    if (deltaY < 0) {
        rowsDelta += 1;
    } else if (deltaY > 0) {
        rowsDelta += -1;
    }

    scrollRightByDelta(rowsDelta)
}

function scrollRightByDelta(rowsDelta) {
    if (rowsDelta == 0 || rightFirst + rowsDelta <= 0 || rightLast + rowsDelta > rightLineCount + downOver) rowsDelta = 0;

    if (rowsDelta != 0) {
        if (rowsDelta > 0) {
            var row;
            if (leftLast != leftLineCount + downOver) {
                for (row = rightFirst; row <= rightFirst + maxRow; row++) {
                    if (leftRealLineByLine[rightLineByRealLine[row].number].number == leftRealLineByLine[rightLineByRealLine[row].number + 1].number) {
                        if (rightLineByRealLine[row].scrolled == false) {
                            rightLineByRealLine[row].scrolled = true;
                            row = -1;
                            break;
                        }
                    }
                }
            } else {
                for (row = rightFirst + maxRow; row <= rightLast; row++) {
                    if (leftRealLineByLine[rightLineByRealLine[row].number].number == leftRealLineByLine[rightLineByRealLine[row].number + 1].number) {
                        if (rightLineByRealLine[row].scrolled == false) {
                            rightLineByRealLine[row].scrolled = true;
                            row = -1;
                            break;
                        }
                    }
                }
            }
            if (row != -1) row--;
        } else {
            if (leftFirst != 1) {
                for (row = rightLast; row >= rightFirst + maxRow - 1; row--) {
                    if (row <= rightLast - downOver) {
                        if (rightLineByRealLine[row].scrolled == true) {
                            rightLineByRealLine[row].scrolled = false;
                            row = -1;
                            break;
                        }
                    }
                }
            } else {
                for (row = rightFirst + maxRow - 1; row >= rightFirst; row--) {
                    if (row <= rightLast - downOver) {
                        if (rightLineByRealLine[row].scrolled == true) {
                            rightLineByRealLine[row].scrolled = false;
                            row = -1;
                            break;
                        }
                    }
                }
            }
            if (row != -1) row++;
        }
        if (row != -1) {
            setLeftRow((rightLineByRealLine[row].number - leftLineByRealLine[leftFirst + (row - rightFirst)].number) + rowsDelta);
            setScrolledLeft(row);
        }
        setRightRow(rowsDelta);
    }

    $('#right-out').html('~~~~~' + ':' + rightFirst + ':' + rightLast + ':' + rightLineCount);

    event.preventDefault();

    writeConnections();
}

function setRightRow(rowsDelta) {
    if (rowsDelta == 0) return;

    var c = $('#right-content').offset();
    var m = $('#right-middle').offset();
    var r = $('#right-line').offset();
    var s = $('#right-scroller').offset();

    $('#right-content').offset({top: c.top - 16 * rowsDelta, left: c.left});
    $('#right-middle').offset({top: m.top - 16 * rowsDelta, left: m.left});
    $('#right-line').offset({top: r.top - 16 * rowsDelta, left: r.left});
    $('#right-scroller').offset({top: s.top + (640 / (rightLineCount + downOver) * rowsDelta), left: s.left});

    rightFirst += rowsDelta;
    rightLast += rowsDelta;
}

function setScrolledRight(row) {
    var r = rightRealLineByLine[leftLineByRealLine[row].number].number;
    for (var i = 1; i < rightLineCount; i++) {
        if (leftRealLineByLine[rightLineByRealLine[i].number].number == leftRealLineByLine[rightLineByRealLine[i].number + 1].number) {
            rightLineByRealLine[i].scrolled = i < r;
//            $('#middle-right-' + i).html((i < r) + '');
        }
    }
}

function writeConnections() {
    var example = document.getElementById("middle");
    var ctx = example.getContext('2d');

    ctx.fillStyle = "#f0f0f0";
    ctx.fillRect(0, 0, 30, 640);

    var lines = _diff.left;

    var inside = false;
    var action = '';

    var x1, y1, x2, y2, x3, y3, x4, y4;

    for (var i = 0; i < lines.length; i++) {
        var l = lines[i];
        if (typeof l.action != 'undefined') {
            if (inside && action != l.action) {
                x4 = 0.5;
                y4 = (leftRealLineByLine[l.number].number - leftFirst) * 16 - 0.5;
                x3 = 29.5;
                y3 = (rightRealLineByLine[l.number].number - rightFirst) * 16 - 0.5;
                fill(ctx, action, x1, y1, x2, y2, x3, y3, x4, y4);
                inside = false;
            }

            if (!inside) {
                inside = true;
                action = l.action;
                x1 = 0.5;
                y1 = (leftRealLineByLine[l.number].number - leftFirst) * 16 - 0.5;
                x2 = 29.5;
                y2 = (rightRealLineByLine[l.number].number - rightFirst) * 16 - 0.5;
            }
        } else {
            if (inside) {
                x4 = 0.5;
                y4 = (leftRealLineByLine[l.number].number - leftFirst) * 16 - 0.5;
                x3 = 29.5;
                y3 = (rightRealLineByLine[l.number].number - rightFirst) * 16 - 0.5;
                fill(ctx, action, x1, y1, x2, y2, x3, y3, x4, y4);
                inside = false;
            }
        }
    }
}

function fill(ctx, action, x1, y1, x2, y2, x3, y3, x4, y4) {
    ctx.beginPath();

    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.lineTo(x3, y3);
    ctx.lineTo(x4, y4);
    ctx.lineTo(x1, y1);

    if (action == '-') ctx.fillStyle = "#a9a9a9";
    if (action == '+') ctx.fillStyle = "#98fcb7";
    if (action == '!') ctx.fillStyle = "#bfd5ff";

    ctx.fill();

    ctx.closePath();

    ctx.beginPath();

    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.moveTo(x3, y3);
    ctx.lineTo(x4, y4);

    ctx.strokeStyle = "#acacac";

    ctx.stroke();

    ctx.closePath();
}

$(document).keydown(function (event) {
    if (event.keyCode == 17) ctrPressed = true;
    if (event.keyCode == 68 && ctrPressed) {
        showDiff($('.active').attr('action'), $('.active').attr('file'), $('.active').attr('rev'));
    }
});

$(document).keyup(function (event) {
    if (event.keyCode == 17) ctrPressed = false;
});

function loadTestDiff() {
    $.get('review/diff?file=' + encodeURIComponent('/app-framework/trunk/app-core/src/main/java/com/shc/obu/app/framework/flow/ItemSearchIncrementalIndexFlow.java') + '&rev=' + '124714', function (data) {
        diff(data);
    })
}
</script>
</head>
<body style="margin: 4px;">

<div style="font-size: 9px;">

    <div id="log" style="float: left;"></div>

    <div id="revision" style="float: left; border: solid 1px; margin-left: 50px;"></div>

</div>

<div style="clear: both; margin-bottom: 4px;"></div>

<div id="diff">

    <div id="left" style="width: 654px; height: 639px; float: left; border: solid 1px #acacac; overflow: hidden;">
        <div id="left-scroll"
             style="width: 20px; height: 639px; background-color: #f9f9f9; float: left; border-right: solid 1px #e6e6e6; position: relative;"></div>
        <div id="left-line"
             style="float: right; background-color: #f3f3f3; border-left: dotted 1px #acacac; direction: rtl;"></div>
        <div id="left-middle"
             style="width: 30px; height: 639px; background-color: #f3f3f3; float: right; border-left: dotted 1px #acacac;"></div>
        <div id="left-content" style="overflow: hidden;"></div>
    </div>

    <canvas id="middle" width="30" height="639"
            style="float: left; border-top: solid 1px #acacac; border-bottom: solid 1px #acacac;"></canvas>

    <div id="right"
         style="width: 654px; height: 639px; float: left; border: solid 1px #acacac; overflow: hidden;">
        <div id="right-line"
             style="background-color: #f3f3f3; float: left; border-right: dotted 1px #acacac; direction: rtl;"></div>
        <div id="right-middle"
             style="width: 30px; height: 639px; background-color: #f3f3f3; float: left; border-right: dotted 1px #acacac;"></div>
        <div id="right-scroll"
             style="width: 20px; height: 639px; background-color: #f9f9f9; float: right; border-left: solid 1px #e6e6e6; position: relative;"></div>
        <div id="right-content" style="overflow: hidden;"></div>
    </div>

</div>

<div>
    <div id="left-out" style="width: 656px; float: left;">left-out</div>
    <div id="right-out" style="width: 656px; float: left; margin-left: 30px;">right-out</div>
</div>

<input type="button" onclick="loadLog();"/>
<input type="button" onclick="loadTestDiff();"/>

</body>
</html>