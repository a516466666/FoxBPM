/**
 * Copyright 1996-2014 FoxBPM ORG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author MAENLIANG
 */
package org.foxbpm.engine.impl.diagramview.svg.builder;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.foxbpm.engine.exception.FoxBPMException;
import org.foxbpm.engine.impl.diagramview.svg.Point;
import org.foxbpm.engine.impl.diagramview.svg.SVGUtils;
import org.foxbpm.engine.impl.diagramview.svg.vo.CircleVO;
import org.foxbpm.engine.impl.diagramview.svg.vo.DefsVO;
import org.foxbpm.engine.impl.diagramview.svg.vo.GVO;
import org.foxbpm.engine.impl.diagramview.svg.vo.LinearGradient;
import org.foxbpm.engine.impl.diagramview.svg.vo.PathVO;
import org.foxbpm.engine.impl.diagramview.svg.vo.StopVO;
import org.foxbpm.engine.impl.diagramview.svg.vo.SvgVO;
import org.foxbpm.engine.impl.util.StringUtil;

/**
 * 
 * 
 * BoundaryEventSVGBuilder 边界时间构造
 * 
 * kin kin 2014年7月22日 下午4:01:54
 * 
 * @version 1.0.0
 * 
 */
public class BoundaryEventSVGBuilder extends AbstractSVGBuilder {
	private static final String FILL_DEFAULT = "ffffff";
	/**
	 * 事件子类型对象
	 */
	private PathVO pathVo;
	/**
	 * 事件圆圈对象
	 */
	private CircleVO circleVO1;

	private CircleVO circleVO2;

	private CircleVO circleVO3;
	/**
	 * 事件节点Builder，获取Circle对象
	 * 
	 * @param voNode
	 */
	public BoundaryEventSVGBuilder(SvgVO voNode) {
		super(voNode);
		this.circleVO1 = SVGUtils.getDefinitionEventVOFromSvgVO(voNode, "bg_frame_1");
		this.circleVO2 = SVGUtils.getDefinitionEventVOFromSvgVO(voNode, "bg_frame_2");
		this.circleVO3 = SVGUtils.getDefinitionEventVOFromSvgVO(voNode, "bg_frame_3");
		if (this.circleVO1 == null || this.circleVO2 == null || this.circleVO3 == null) {
			throw new FoxBPMException("EventSVGBuilder构造 EVENT SVG时，无法获取圆形对象");
		}
		List<GVO> gVoList = this.svgVo.getgVo().getgVoList();
		Iterator<GVO> gvoIter = gVoList.iterator();
		while (gvoIter.hasNext()) {
			GVO next = gvoIter.next();
			List<PathVO> pathVoList = next.getPathVoList();
			if (pathVoList != null && pathVoList.size() > 0
					&& StringUtil.equals(pathVoList.get(0).getId(), "path1")) {
				pathVo = pathVoList.get(0);
			}
		}
	}
	public BoundaryEventSVGBuilder(SvgVO voNode, boolean isInterrupt) {
		this(voNode);
		if (!isInterrupt) {
			this.circleVO1.setStyle("stroke-dasharray: 5.5, 3");
			this.circleVO2.setStyle("stroke-dasharray: 4.5, 3");
		}
	}

	public void setTypeStyle(String typeStyle) {
		if (pathVo == null) {
			return;
		}
		pathVo.setStyle(typeStyle);
	}

	 
	public void setTypeStroke(String stroke) {
		if (pathVo == null) {
			return;
		}
		this.pathVo.setStroke(stroke);
	}

	 
	public void setTypeStrokeWidth(float strokeWidth) {
		if (pathVo == null) {
			return;
		}
		this.pathVo.setStrokeWidth(strokeWidth);
	}

	 
	public void setTypeFill(String fill) {
		if (pathVo == null) {
			return;
		}
		this.pathVo.setFill(fill);
	}

	 
	public void setWidth(float width) {
		float tempWidth = width / 2;
		this.circleVO1.setR(tempWidth);
		this.circleVO2.setR(tempWidth - 3);
		this.circleVO3.setR(tempWidth - 8);

	}

	 
	public void setStroke(String stroke) {
		if (StringUtils.isBlank(stroke)) {
			this.circleVO1.setStroke(STROKE_DEFAULT);
			this.circleVO2.setStroke(STROKE_DEFAULT);
			this.circleVO3.setStroke(STROKE_DEFAULT);
			return;
		}
		this.circleVO1.setStroke(COLOR_FLAG + stroke);
		this.circleVO2.setStroke(COLOR_FLAG + stroke);
		this.circleVO3.setStroke(COLOR_FLAG + stroke);
	}

	 
	public void setStrokeWidth(float strokeWidth) {
		this.circleVO1.setStrokeWidth(strokeWidth);
		this.circleVO2.setStrokeWidth(strokeWidth);
		this.circleVO3.setStrokeWidth(strokeWidth);

	}

	 
	public void setFill(String fill) {
		if (StringUtils.isBlank(fill)) {
			fill = FILL_DEFAULT;
		}
		DefsVO defsVo = this.svgVo.getgVo().getDefsVo();
		if (defsVo != null) {
			LinearGradient linearGradient = defsVo.getLinearGradient();
			if (linearGradient != null) {
				String backGroudUUID = UUID.randomUUID().toString();
				linearGradient.setId(backGroudUUID);
				Float cx = this.circleVO1.getCx();
				Float cy = this.circleVO1.getCy();
				Float r = this.circleVO1.getR();
				linearGradient.setX1(cx - r);
				linearGradient.setX2(cx - r);
				linearGradient.setY1(cy - r);
				linearGradient.setY2(cy + r);
				List<StopVO> stopVoList = linearGradient.getStopVoList();
				if (stopVoList != null && stopVoList.size() > 0) {
					StopVO stopVO = stopVoList.get(LINEARGRADIENT_INDEX);
					this.circleVO1.setFill(new StringBuffer(BACK_GROUND_PREFIX)
							.append(backGroudUUID).append(BRACKET_SUFFIX).toString());
					stopVO.setStopColor(COLOR_FLAG + fill);
					return;
				}

			}
		}
	}

	 
	public void setID(String id) {
		this.circleVO1.setId(id);
	}

	 
	public void setName(String name) {
		this.circleVO1.setName(name);
	}

	 
	public void setStyle(String style) {
		this.circleVO1.setStyle(style);
	}

	/**
	 * @TODO 圆心坐标设置是绝对坐标值，后期如果需要添加子类型，则采用transform的形式
	 * 
	 */
	 
	public void setXAndY(float x, float y) {
		// 流程图定义的是圆对应矩形左上角的坐标，所以对应的SVG坐标需要将坐标值加半径
		// 如果是事件节点，字体横坐标和圆心的横坐标一直，纵坐标等圆心坐标值加圆的半径值
		// 如果存在子类型，例如ERROR
		if (this.pathVo != null) {
			// 整体 SHIFT
			this.svgVo.getgVo().setTransform(
					new StringBuffer(TRANSLANT_PREFIX).append(x).append(COMMA).append(y)
							.append(BRACKET_SUFFIX).toString());
			// TODO 同时需要设置文本的相对偏移量
			String elementValue = textVO.getElementValue();
			if (StringUtils.isNotBlank(elementValue)) {
				int textWidth = SVGUtils.getTextWidth(this.textVO.getFont(), elementValue);
				if (SVGUtils.isChinese(elementValue.charAt(0))) {
					textWidth = textWidth + (textWidth / 20) * 5;
				}
				this.setTextX(-textWidth/2);
				this.setTextY(this.circleVO1.getR() + 35);
			}
			
		} else {
			this.circleVO1.setCx(x);
			this.circleVO1.setCy(y);
			String elementValue = textVO.getElementValue();
			if (StringUtils.isNotBlank(elementValue)) {
				int textWidth = SVGUtils.getTextWidth(this.textVO.getFont(), elementValue);
				if (SVGUtils.isChinese(elementValue.charAt(0))) {
					textWidth = textWidth + (textWidth / 20) * 5;
				}
				this.setTextX(x - textWidth / 2);
				this.setTextY(y + this.circleVO1.getR() + 15);
			}
		}
	}

	/**
	 * 圆圈不需要设置拐点
	 */
	public void setWayPoints(List<Point> pointList) {
		// TODO Auto-generated method stub

	}

	/**
	 * 圆圈已经设置半径，不需要在设置高度
	 */
	public void setHeight(float height) {
		// TODO Auto-generated method stub

	}

}
